package me.August.MyRPC.config;

import lombok.Data;
import me.August.MyRPC.discovery.RegistryConfig;
import me.August.MyRPC.utils.IdGenerator;

@Data
public class Configuration {

    // 配置信息
    // 应用程序的名字
    private String appName = "default";
    // 端口号
    private int port = 8094;
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


}
