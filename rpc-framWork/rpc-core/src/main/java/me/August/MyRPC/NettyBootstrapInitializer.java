package me.August.MyRPC;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.channelHandler.ConsumerChannelInitializer;

// 获取单例bootstrap
@Slf4j
public class NettyBootstrapInitializer {
    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                // 选择初始化一个什么样的channel
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    private NettyBootstrapInitializer() {
    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
