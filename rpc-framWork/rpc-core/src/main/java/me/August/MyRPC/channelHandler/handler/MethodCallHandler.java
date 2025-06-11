package me.August.MyRPC.channelHandler.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.ServiceConfig;
import me.August.MyRPC.enumeration.RequestType;
import me.August.MyRPC.enumeration.RespCode;
import me.August.MyRPC.protection.RateLimiter;
import me.August.MyRPC.protection.TokenBuketRateLimiter;
import me.August.MyRPC.transport.message.RequestPayload;
import me.August.MyRPC.transport.message.RpcRequest;
import me.August.MyRPC.transport.message.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @Author 9August
 * @Date 2025/5/31 23:10
 * @description:
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        rpcResponse.setCompressType(rpcRequest.getCompressType());
        rpcResponse.setSerializeType(rpcRequest.getSerializeType());

        Channel channel = channelHandlerContext.channel();

        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter =
                RpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();

        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBuketRateLimiter(10, 10);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();

        // 5、处理请求的逻辑
        // 限流
        if (!allowRequest) {
            // 需要封装响应并且返回了
            rpcResponse.setCode(RespCode.RATE_LIMIT.getCode());
        } else if (rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()) {
            // 需要封装响应并且返回
            rpcResponse.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());
            // 正常调用
        }else {
            // 1. 获取负载内容然后进行方法调用
            RequestPayload requestPayload = rpcRequest.getRequestPayload();
            try {
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用。", rpcRequest.getRequestId());
                }
                // 封装响应   我们是否需要考虑另外一个问题，响应码，响应类型
                rpcResponse.setCode(RespCode.SUCCESS.getCode());
                rpcResponse.setBody(result);
            } catch (Exception e){
                log.error("编号为【{}】的请求在调用过程中发生异常。",rpcRequest.getRequestId(),e);
                rpcResponse.setCode(RespCode.FAIL.getCode());
            }
        }
        // 4. 返回调用结果
        channelHandlerContext.channel().writeAndFlush(rpcResponse);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        // 寻找到匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = RpcBootstrap.SERVERS_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        // 通过反射调用 1、获取方法对象  2、执行invoke方法
        Object returnValue;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("调用服务【{}】的方法【{}】时发生了异常。", interfaceName, methodName, e);
            throw new RuntimeException(e);
        }
        return returnValue;

    }
}
