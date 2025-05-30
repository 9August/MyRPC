package me.August.MyRPC.discovery;

import me.August.MyRPC.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

public interface Registry {
    //注册使用的注册中心
    void register(ServiceConfig<?> serviceConfig);

    // 注册中心拉取服务列表
    List<InetSocketAddress> lookup(String serviceName, String group);

}
