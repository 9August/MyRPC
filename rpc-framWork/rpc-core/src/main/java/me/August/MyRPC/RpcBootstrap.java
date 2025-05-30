package me.August.MyRPC;


import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.config.Configuration;
import me.August.MyRPC.discovery.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcBootstrap {
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    // 全局的配置中心
    private final Configuration configuration;

    public final static Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);


    private RpcBootstrap() {
        configuration = new Configuration() ;
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

    public RpcBootstrap scan(String s) {
        return this;
    }

    public RpcBootstrap compress(String gzip) {
        return this;
    }

    public RpcBootstrap group(String primary) {
        return this;
    }

    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        // 1、reference需要一个注册中心
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        reference.setGroup(this.getConfiguration().getGroup());

        return this;
    }

    // 启动netty服务
    public void start() {


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
