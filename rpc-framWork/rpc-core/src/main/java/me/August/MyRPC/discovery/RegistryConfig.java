package me.August.MyRPC.discovery;

import me.August.MyRPC.constants.Constant;
import me.August.MyRPC.discovery.impl.ZookeeperRegistry;
import me.August.MyRPC.exceptons.DiscoveryException;

public class RegistryConfig {
    private final String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    // 获取注册中心实例
    public Registry getRegistry() {
        // 1、获取注册中心的类型
        String registryType = getRegistryType(connectString,true).toLowerCase().trim();
        // 2、通过类型获取具体注册中心
        if( registryType.equals("zookeeper") ){
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        } else if (registryType.equals("nacos")){
            String host = getRegistryType(connectString, false);
//            return new NacosRegistry(host, Constant.TIME_OUT);
        }
        throw new DiscoveryException("未发现合适的注册中心。");
    }

    private String getRegistryType(String connectString,boolean ifType){
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length != 2){
            throw new RuntimeException("给定的注册中心连接url不合法");
        }
        if(ifType){
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }
}
