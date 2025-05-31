package me.August.MyRPC.channelHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import me.August.MyRPC.channelHandler.handler.TestSimpleChannelInboundHandler;

/**
 * @Author 9August
 * @Date 2025/5/31 17:32
 * @description: 进行通道初始化配置
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new TestSimpleChannelInboundHandler());
    }
}
