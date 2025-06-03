package me.August.MyRPC.loadBalancer;

import java.net.InetSocketAddress;

/**
 * @Author 9August
 * @Date 2025/6/2 14:57
 * @description:
 */
public interface Selector {
    // 根据服务列表执行一种算法获取一个服务节点
    InetSocketAddress getNext();
}
