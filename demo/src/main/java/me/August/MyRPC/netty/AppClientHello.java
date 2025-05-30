package me.August.MyRPC.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class AppClientHello {
    private final String host;
    private final int port;

    public AppClientHello(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        //定义干活的线程池，I/O线程池
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bs = new Bootstrap();//客户端辅助启动类
            bs.group(group)
                    .channel(NioSocketChannel.class)//实例化一个Channel
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>()//进行通道初始化配置
                    {
                        @Override
                        protected void initChannel(SocketChannel socketChannel)
                                throws Exception {
                            socketChannel.pipeline().addLast(new HandlerClientHello());//添加我们自定义的Handler
                        }
                    });
            //连接到远程节点；等待连接完成
            ChannelFuture future = bs.connect().sync();
            //发送消息到服务器端，编码格式是utf-8
            future.channel().writeAndFlush(Unpooled.copiedBuffer("Hello World",
                    CharsetUtil.UTF_8));
            //阻塞操作，closeFuture()开启了一个channel的监听器（这期间channel在进行各项工作），直到链路断开
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        new AppClientHello("127.0.0.1", 8080).run();
    }

}
