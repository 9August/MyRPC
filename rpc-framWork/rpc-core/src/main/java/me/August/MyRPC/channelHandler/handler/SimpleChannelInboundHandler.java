package me.August.MyRPC.channelHandler.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.enumeration.RespCode;
import me.August.MyRPC.exceptons.ResponseException;
import me.August.MyRPC.transport.message.RpcResponse;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @Author 9August
 * @Date 2025/5/31 17:26
 * @description: 客户端最后处理调用结果Handler
 */
@Slf4j
public class SimpleChannelInboundHandler extends io.netty.channel.SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        // 从全局的挂起的请求中寻找与之匹配的待处理的completableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(rpcResponse.getRequestId());
        Object returnValue = rpcResponse.getBody();
        completableFuture.complete(returnValue);

        if (log.isDebugEnabled()) {
            log.debug("响应【{}】已经在调用端完成相应响应结果处理。", rpcResponse.getRequestId());
        }
    }
}
