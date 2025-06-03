package me.August.MyRPC;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.annotation.RpcApi;
import me.August.MyRPC.channelHandler.handler.MethodCallHandler;
import me.August.MyRPC.channelHandler.handler.RpcRequestDecoder;
import me.August.MyRPC.channelHandler.handler.RpcResponseEncoder;
import me.August.MyRPC.config.Configuration;
import me.August.MyRPC.core.HeartbeatDetector;
import me.August.MyRPC.discovery.RegistryConfig;
import me.August.MyRPC.transport.message.RpcRequest;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class RpcBootstrap {
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    // 全局的配置中心
    private final Configuration configuration;

    // 连接的缓存,如果使用InetSocketAddress这样的类做key，一定要看他有没有重写equals方法和toString方法
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    // 维护已经发布且暴露服务列表
    public final static Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);

    // 定义全局的对外挂起的 completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);
    // 保存request对象，可以到当前线程中随时获取
    public static final ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    //
    public final static TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();


    private RpcBootstrap() {
        configuration = new Configuration();
    }

    // 单例模式
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    public RpcBootstrap application(String appName) {
        configuration.setAppName(appName);
        return this;
    }

    // 注册到Zookeeper
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        return this;
    }


    // 协议封装
    public RpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()) {
            log.debug("我们配置了使用的序列化的方式为【{}】.", serializeType);
        }
        return this;
    }

    public RpcBootstrap scan(String packageName) {
        // 1. 通过packageName获取其下的所有类的全限定名称
        List<String> classNames = getAllClassNames(packageName);
        System.out.println(classNames);
        // 2. 通过反射获取接口，构建具体实现
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException();
                    }
                }).filter(clazz -> clazz.getAnnotation(RpcApi.class) != null)
                .collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            // 获取他的接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                if (log.isDebugEnabled()){
                    log.debug("---->已经通过包扫描，将服务【{}】发布.",anInterface);
                }
                // 3、发布
                publish(serviceConfig);
            }
        }

        return this;
    }


    private List<String> getAllClassNames(String packageName) {
        //

        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫描时，发现路径不存在.");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        recursionFile(absolutePath, classNames, basePath);
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        // 获取文件
        File file = new File(absolutePath);
        if (file.isDirectory()) {
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children == null || children.length == 0) {
                return classNames;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    // 递归调用
                    recursionFile(child.getAbsolutePath(), classNames, basePath);
                } else {
                    // 文件 --> 类的全限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    classNames.add(className);
                }
            }
        } else {
            // 文件 --> 类的全限定名称
            String className = getClassNameByAbsolutePath(absolutePath, basePath);
            classNames.add(className);
        }
        return classNames;
    }

    // 获取类的全限定名字
    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\")))
                .replaceAll("\\\\", ".");
        fileName = fileName.substring(0, fileName.indexOf(".class"));
        return fileName;
    }


    public RpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()) {
            log.debug("我们配置了使用的压缩算法为【{}】.", compressType);
        }
        return this;
    }

    public RpcBootstrap group(String primary) {
        return this;
    }

    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 开启对这个服务的心跳检测
        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName());

        // 1、reference需要一个注册中心
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        reference.setGroup(this.getConfiguration().getGroup());

        return this;
    }

    // 启动netty服务
    public void start() {

        //Netty的Reactor线程池，初始化了一个NioEventLoop数组，用来处理I/O操作,如接受新的连接和读写数据
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();//用于启动NIO服务
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class) //实例化一个channel用于建立连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //ChannelInitializer配置一个新的Channel,用于把自定义的处理类增加到pipline上来
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //配置childHandler来通知一个关于消息处理的InfoServerHandler实例
                            // 核心，添加入栈和出栈的处理
                            ch.pipeline()
                                    // 日志
                                    .addLast(new LoggingHandler())
                                    // 报文解码
                                    .addLast(new RpcRequestDecoder())
                                    // 调用服务，返回结果
                                    .addLast(new MethodCallHandler())
                                    // 封装响应
                                    .addLast(new RpcResponseEncoder())
                            ;
                        }
                    });
            //绑定服务器，该实例将提供有关IO操作的结果或状态的信息
            ChannelFuture channelFuture = b.bind(configuration.getPort()).sync();
            System.out.println("在" + channelFuture.channel().localAddress() + "上开启监听");

            //阻塞操作，closeFuture()开启了一个channel的监听器（这期间channel在进行各项工作），直到链路断开
            // closeFuture().sync()会阻塞当前线程，直到通道关闭操作完成。这可以用于确保在关闭通道之前，程序不会提前退出。
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {  //关闭EventLoopGroup并释放所有资源，包括所有创建的线程
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // 注册服务到服务中心
    public RpcBootstrap publish(ServiceConfig<?> service) {
        configuration.getRegistryConfig().getRegistry().register(service);
        SERVERS_LIST.put(service.getInterface().getName(), service);
        return this;
    }


    public Configuration getConfiguration() {
        return configuration;
    }

}
