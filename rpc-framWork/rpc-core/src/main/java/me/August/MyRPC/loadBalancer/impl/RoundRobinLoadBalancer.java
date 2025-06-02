package me.August.MyRPC.loadBalancer.impl;

import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.exceptons.LoadBalancerException;
import me.August.MyRPC.loadBalancer.AbstractLoadBalancer;
import me.August.MyRPC.loadBalancer.LoadBalancer;
import me.August.MyRPC.loadBalancer.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author 9August
 * @Date 2025/6/2 14:53
 * @description: 轮询的负载均衡策略
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }


    private static class RoundRobinSelector implements Selector {
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.size() == 0) {
                log.error("进行负载均衡选取节点时发现服务列表为空.");
                throw new LoadBalancerException();
            }
            InetSocketAddress address = serviceList.get(index.get());
            // 如果他到了最后的一个位置，重置
            if (index.get() == serviceList.size() - 1) {
                index.set(0);
            } else {
                // 游标后移一位
                index.incrementAndGet();
            }
            return address;
        }
    }

}
