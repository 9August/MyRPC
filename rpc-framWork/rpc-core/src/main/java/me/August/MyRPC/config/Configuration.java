package me.August.MyRPC.config;

import lombok.Data;
import me.August.MyRPC.discovery.RegistryConfig;
import me.August.MyRPC.loadBalancer.impl.RoundRobinLoadBalancer;
import me.August.MyRPC.utils.IdGenerator;
import me.August.MyRPC.loadBalancer.LoadBalancer;

/*
 * 实现全局配置，优先级设为 代码配置 > xml配置 > spi配置(后续实现) > 默认配置
 * */
@Data
public class Configuration {

    // 配置信息
    // 应用程序的名字
    private String appName = "default";
    // 端口号
    private int port = 8097;
    // 使用的注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
    //序列化协议
    private String serializeType = "jdk";
    // 分组信息
    private String group = "default";
    // id发射器(雪花生成算法)
    public IdGenerator idGenerator = new IdGenerator(1, 2);
    // 压缩使用的协议
    private String compressType = "gzip";
    // 配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    public Configuration() {
        // 1、成员变量的默认配置项
        // 2、spi机制发现相关配置项
        // 3、读取xml获得上边的信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);

        // 4、编程配置项，yrpcBootstrap提供
    }

}
