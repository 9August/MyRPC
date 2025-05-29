package me.August.MyRPC;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    // 分组信息
    private String group;

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};

        // 动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("hello proxy");
                return null;
            }
        });

        return (T) helloProxy;
    }
}
