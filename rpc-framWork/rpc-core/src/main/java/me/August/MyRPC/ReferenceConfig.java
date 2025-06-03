package me.August.MyRPC;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.constants.Constant;
import me.August.MyRPC.discovery.Registry;
import me.August.MyRPC.discovery.impl.ZookeeperRegistry;
import me.August.MyRPC.exceptons.NetworkException;
import me.August.MyRPC.proxy.handler.RpcConsumerInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    private Registry registry;

    // 分组信息
    private String group;

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }
    public Class<T> getInterface() {
        return interfaceRef;
    }
    ZookeeperRegistry zookeeperRegistry;

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};

        // 动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes,
                new RpcConsumerInvocationHandler(registry, interfaceRef, group));

        return (T) helloProxy;
    }

}
