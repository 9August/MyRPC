package me.August.MyRPC.channelHandler.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.ServiceConfig;
import me.August.MyRPC.transport.message.RequestPayload;
import me.August.MyRPC.transport.message.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author 9August
 * @Date 2025/5/31 23:10
 * @description:
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        // 1. 获取负载内容
        RequestPayload requestPayload = rpcRequest.getRequestPayload();
        // 2. 根据负载进行方法调用
        Object object = callTargetMethod(requestPayload);
        // 3. 封装响应

        // 4. 返回调用结果
        channelHandlerContext.channel().writeAndFlush(object);


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
