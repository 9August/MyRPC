package me.August.MyRPC.proxy.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.NettyBootstrapInitializer;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.discovery.Registry;
import me.August.MyRPC.exceptons.DiscoveryException;
import me.August.MyRPC.exceptons.NetworkException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
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

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef,String group) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
        this.group = group;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 寻找可用服务
        List<InetSocketAddress> addresses = registry.lookup(interfaceRef.getName(), group);
        InetSocketAddress address = addresses.get(0);
        log.debug("服务调用方,发现了服务{}的可用主机【{}】", interfaceRef.getName(), address);

        // 2. 获取channel
        Channel channel = getAvailableChannel(address);

        // 3. 发送请求
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        RpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);
        channel.writeAndFlush(Unpooled.copiedBuffer("客户端说hello".getBytes()))
                .addListener(
                        (ChannelFutureListener) promise -> {
                            if (!promise.isSuccess()) {
                                completableFuture.completeExceptionally(promise.cause());
                            }
                        }
                );
        // 4. 获取异步结果
        return completableFuture.get(3, TimeUnit.SECONDS);
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
                            if (log.isDebugEnabled()) {
                                log.debug("已经和【{}】建立了连接。", address);
                            }
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
