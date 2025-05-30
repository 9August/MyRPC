package me.August.MyRPC;

import me.August.MyRPC.constants.Constant;
import org.apache.zookeeper.ZooKeeper;

public class RegistryConfig {
    private final String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    public ZooKeeper getRegistry() {
        // 1、获取注册中心的类型
        String registryType = getRegistryType(connectString,true).toLowerCase().trim();

        // 2、通过类型获取具体注册中心
        if( registryType.equals("zookeeper") ){
            String host = getRegistryType(connectString, false);
//            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        }
        return null;
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
