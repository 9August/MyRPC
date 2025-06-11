package me.August.MyRPC.proxy.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.NettyBootstrapInitializer;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.annotation.TryTimes;
import me.August.MyRPC.compress.CompressorFactory;
import me.August.MyRPC.discovery.Registry;
import me.August.MyRPC.enumeration.RequestType;
import me.August.MyRPC.exceptons.DiscoveryException;
import me.August.MyRPC.exceptons.NetworkException;
import me.August.MyRPC.protection.CircuitBreaker;
import me.August.MyRPC.serialize.SerializerFactory;
import me.August.MyRPC.transport.message.RequestPayload;
import me.August.MyRPC.transport.message.RpcRequest;
import me.August.MyRPC.utils.IdGenerator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
 * 客户端通信逻辑
 * 1. 寻找可用服务（zookeeper） 2. 建立连接（netty） 3.发送请求 4. 异步得到结果
 * */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    //注册中心和一个接口
    private final Registry registry;
    private final Class<?> interfaceRef;
    private String group;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef, String group) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
        this.group = group;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 从接口中获取判断是否需要重试
        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);
        // 默认值0,代表不重试
        int tryTimes = 0;
        int intervalTime = 0;

        if (tryTimesAnnotation != null) {
            tryTimes = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.intervalTime();
        }
        while (true) {
            RequestPayload requestPayload = RequestPayload.builder()
                    .interfaceName(interfaceRef.getName())
                    .methodName(method.getName())
                    .parametersType(method.getParameterTypes())
                    .parametersValue(args)
                    .returnType(method.getReturnType())
                    .build();

            RpcRequest rpcRequest = RpcRequest.builder()
                    .requestId(RpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                    .compressType(CompressorFactory.getCompressor(RpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                    .requestType(RequestType.REQUEST.getId())
                    .serializeType(SerializerFactory.getSerializer(RpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                    .timeStamp(System.currentTimeMillis())
                    .requestPayload(requestPayload)
                    .build();

            RpcBootstrap.REQUEST_THREAD_LOCAL.set(rpcRequest);
            // 1. 使用负载均衡器获取可用服务
            InetSocketAddress address = RpcBootstrap.getInstance()
                    .getConfiguration().getLoadBalancer().selectServiceAddress(interfaceRef.getName(), group);
            if (log.isDebugEnabled()) {
                log.debug("服务调用方，发现了服务【{}】的可用主机【{}】.",
                        interfaceRef.getName(), address);
            }

            // 4、获取当前地址所对应的断路器，如果断路器是打开的,则不发送请求，抛出异常
            Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = RpcBootstrap.getInstance()
                    .getConfiguration().getEveryIpCircuitBreaker();
            CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(address);
            if (circuitBreaker == null) {
                circuitBreaker = new CircuitBreaker(10, 0.5F);
                everyIpCircuitBreaker.put(address, circuitBreaker);
            }

            try {
                // 如果断路器是打开的
                if (rpcRequest.getRequestType() != RequestType.HEART_BEAT.getId() && circuitBreaker.isBreak()) {
                    // 定期打开
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            RpcBootstrap.getInstance()
                                    .getConfiguration().getEveryIpCircuitBreaker()
                                    .get(address).reset();
                        }
                    }, 5000);

                    throw new RuntimeException("当前断路器已经开启，无法发送请求");
                }
                // 2. 获取channel
                Channel channel = getAvailableChannel(address);
                if (log.isDebugEnabled()) {
                    log.debug("已经和【{}】建立了连接，准备发送数据", address);
                }
                // 3. 发送请求
                // 异步全局露出
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                RpcBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);
                // 3.2 请求发送
                channel.writeAndFlush(rpcRequest)
                        .addListener(
                                (ChannelFutureListener) promise -> {
                                    if (!promise.isSuccess()) {
                                        completableFuture.completeExceptionally(promise.cause());
                                    }
                                }
                        );
                // 清理threadLocal
                RpcBootstrap.REQUEST_THREAD_LOCAL.remove();
                // 4. 获取异步结果
                Object result = completableFuture.get(5, TimeUnit.SECONDS);
                circuitBreaker.recordRequest();
                return result;
            } catch (Exception e) {
                // 次数减一，并且等待固定时间，固定时间有一定的问题，重试风暴
                tryTimes--;
                // 记录错误的次数
                circuitBreaker.recordErrorRequest();
                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException ex) {
                    log.error("在进行重试时发生异常.", ex);
                }
                if (tryTimes < 0) {
                    log.error("对方法【{}】进行远程调用时，重试{}次，依然不可调用",
                            method.getName(), tryTimes, e);
                    break;
                }
                log.error("在进行第{}次重试时发生异常.", 3 - tryTimes, e);
            }
        }
        throw new RuntimeException("执行远程方法" + method.getName() + "调用失败。");
    }

    private Channel getAvailableChannel(InetSocketAddress address) {
        // 缓存获取连接
        Channel channel = RpcBootstrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
            // await 方法会阻塞, 等待连接成功返回
            //连接到远程节点；等待连接完成
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener(
                    (ChannelFutureListener) promise -> {
                        if (promise.isDone()) {

                            channelFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    });

            // 阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时，发生异常。", e);
                throw new DiscoveryException(e);
            }
            // 缓存
            RpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            log.error("获取或建立与【{}】的通道时发生了异常。", address);
            throw new NetworkException("获取通道时发生了异常。");
        }
        return channel;
    }
}
