package me.August.MyRPC.channelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.August.MyRPC.RpcBootstrap;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @Author 9August
 * @Date 2025/5/31 17:26
 * @description: 测试Handler
 */
public class TestSimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        // 服务提供方返回给客户端处理的结果
        String res = msg.toString(Charset.defaultCharset());
        // 从全局的挂起请求中与之匹配的待处理的 completableFuture（ReferenceConfig.get最后结果）
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(res);
    }



}
