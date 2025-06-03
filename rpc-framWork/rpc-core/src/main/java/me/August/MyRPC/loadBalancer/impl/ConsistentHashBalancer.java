package me.August.MyRPC.loadBalancer.impl;

import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.exceptons.LoadBalancerException;
import me.August.MyRPC.loadBalancer.AbstractLoadBalancer;
import me.August.MyRPC.loadBalancer.Selector;
import me.August.MyRPC.transport.message.RpcRequest;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author 9August
 * @Date 2025/6/2 14:53
 * @description: 一致性哈希的负载均衡策略
 */
@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }


    private static class ConsistentHashSelector implements Selector {
        // hash环用来存储服务器节点
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> addresses, int virtualNodes) {
            // 将节点转化为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : addresses) {
                // 把每一个节点加入到hash环中
                addNodeToCircle(inetSocketAddress);
            }
        }

        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            // 为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                // 关在到hash环上
                circle.put(hash, inetSocketAddress);
                if (log.isDebugEnabled()) {
                    log.debug("hash为[{}]的节点已经挂载到了哈希环上.", hash);
                }
            }
        }

        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
            // md5得到的结果是一个字节数组，但是我们想要int 4个字节

            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if (digest[i] < 0) {
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }
            }
            return res;
        }


        @Override
        public InetSocketAddress getNext() {
            // hash环已经建立好了，选择请求里的要素来进行hash运算
            RpcRequest rpcRequest = RpcBootstrap.REQUEST_THREAD_LOCAL.get();

            // 根据请求的一些特征来选择服务器
            String requestId = Long.toString(rpcRequest.getRequestId());

            // 使用请求的id做hash
            int hash = hash(requestId);

            // 判断该hash值是否能直接落在一个服务器上，和服务器的hash一样
            if (!circle.containsKey(hash)) {
                // 寻找和当前节点最近的一个节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            return circle.get(hash);
        }
    }

}
