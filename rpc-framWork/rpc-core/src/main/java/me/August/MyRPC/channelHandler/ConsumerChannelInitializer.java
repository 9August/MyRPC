package me.August.MyRPC.channelHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import me.August.MyRPC.channelHandler.handler.RpcRequestEncoder;
import me.August.MyRPC.channelHandler.handler.RpcResponseDecoder;
import me.August.MyRPC.channelHandler.handler.SimpleChannelInboundHandler;

/**
 * @Author 9August
 * @Date 2025/5/31 17:32
 * @description: 进行通道初始化配置
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                // netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 出栈消息编码器
                .addLast(new RpcRequestEncoder())
                // 入栈的解码器
                .addLast(new RpcResponseDecoder())
                // 处理结果
                .addLast(new SimpleChannelInboundHandler());
    }
}
