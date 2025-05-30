package me.August.MyRPC;


import lombok.*;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.constants.Constant;
import me.August.MyRPC.discovery.Registry;
import me.August.MyRPC.discovery.impl.ZookeeperRegistry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;

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
    ZookeeperRegistry zookeeperRegistry;
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};

        // 动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("hello proxy");
                log.info("method->{}",method.getName());
                // 寻找可用服务
                List<InetSocketAddress> addresses = registry.lookup(interfaceRef.getName(), group);
                InetSocketAddress address = addresses.get(0);
                log.debug("服务调用方,发现了服务{}的可用主机{}",interfaceRef.getName(),address);


                return null;
            }
        });

        return (T) helloProxy;
    }

}
