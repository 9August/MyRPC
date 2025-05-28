package me.August.MyRPC.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 添加解码器
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
//        pipeline.addLast(new MyMessageDecoder());
        // 添加编码器
        pipeline.addLast(new LengthFieldPrepender(2));
//        pipeline.addLast(new MyMessageEncoder());
        // 添加业务逻辑处理器
//        pipeline.addLast(new MyBusinessHandler());
    }
}