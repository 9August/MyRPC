package me.August.MyRPC.loadBalancer.impl;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.loadBalancer.AbstractLoadBalancer;
import me.August.MyRPC.loadBalancer.Selector;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author 9August
 * @Date 2025/6/2 22:28
 * @description: 最段响应时间的负载均衡器
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }

    private static class MinimumResponseTimeSelector implements Selector {

        public MinimumResponseTimeSelector(List<InetSocketAddress> addresses) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = RpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("选取了响应时间为【{}ms】的服务节点.", entry.getKey());
                }
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }

            // 直接从缓存中获取一个可用的就行了
            System.out.println("----->" + Arrays.toString(RpcBootstrap.CHANNEL_CACHE.values().toArray()));
            Channel channel = (Channel) RpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }

    }
}
