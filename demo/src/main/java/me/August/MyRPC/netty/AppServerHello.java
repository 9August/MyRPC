package me.August.MyRPC.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class AppServerHello {

    private int port;

    public AppServerHello(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        //Netty的Reactor线程池，初始化了一个NioEventLoop数组，用来处理I/O操作,如接受新的连接和读写数据
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();//用于启动NIO服务
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class) //实例化一个channel用于建立连接
                    .localAddress(new InetSocketAddress(port))//设置监听端口
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //ChannelInitializer配置一个新的Channel,用于把自定义的处理类增加到pipline上来
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //配置childHandler来通知一个关于消息处理的InfoServerHandler实例
                            ch.pipeline().addLast(new HandlerServerHello());
                        }
                    });
            //绑定服务器，该实例将提供有关IO操作的结果或状态的信息
            ChannelFuture channelFuture = b.bind().sync();
            System.out.println("在" + channelFuture.channel().localAddress() + "上开启监听");

            //阻塞操作，closeFuture()开启了一个channel的监听器（这期间channel在进行各项工作），直到链路断开
            // closeFuture().sync()会阻塞当前线程，直到通道关闭操作完成。这可以用于确保在关闭通道之前，程序不会提前退出。
            channelFuture.channel().closeFuture().sync();
        } finally {  //关闭EventLoopGroup并释放所有资源，包括所有创建的线程
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();

        }
    }

    public static void main(String[] args) throws Exception {
        new AppServerHello(8080).run();
    }
}
