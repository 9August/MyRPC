package me.August.MyRPC.loadBalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author 9August
 * @Date 2025/6/2 14:48
 * @description: 根据服务列表找到一个可以用的服务
 */
public interface LoadBalancer {

    // 根据服务名找到一个可以用的服务
    InetSocketAddress selectServiceAddress(String serviceName, String group);

    // 节点，发生动态上下线，重新负载均衡
    void reLoadBalance(String serviceName, List<InetSocketAddress> addresses);
}
