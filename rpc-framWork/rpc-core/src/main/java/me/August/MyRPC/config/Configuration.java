package me.August.MyRPC.config;

import lombok.Data;
import me.August.MyRPC.RegistryConfig;
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


}
