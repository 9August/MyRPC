package me.August.MyRPC.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class HandlerClientHello extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    // 处理接收到的消息
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf
            byteBuf) throws Exception {
        System.out.println("接收到的消息：" + byteBuf.toString(CharsetUtil.UTF_8));
    }

    @Override
    // 处理I/O事件的异常
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}