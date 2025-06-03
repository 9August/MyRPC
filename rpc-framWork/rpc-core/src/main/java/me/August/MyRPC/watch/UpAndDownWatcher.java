package me.August.MyRPC.watch;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.NettyBootstrapInitializer;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.discovery.Registry;
import me.August.MyRPC.loadBalancer.LoadBalancer;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @Author 9August
 * @Date 2025/6/2 23:56
 * @description: 动态感知服务上下线
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        // 当前的阶段是否发生了变化
        if (event.getType() == Event.EventType.NodeChildrenChanged){
            if (log.isDebugEnabled()){
                log.debug("检测到服务【{}】下有节点上/下线，将重新拉取服务列表...",event.getPath());
            }
            String serviceName = getServiceName(event.getPath());
            Registry registry = RpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();;
            List<InetSocketAddress> addresses = registry.lookup(serviceName,
                    RpcBootstrap.getInstance().getConfiguration().getGroup());
            // 处理新增的节点
            // 新增的节点   会在address 不在CHANNEL_CACHE
            for (InetSocketAddress address : addresses) {
                if(!RpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    // 根据地址建立连接，并且缓存
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap()
                                .connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    RpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }

            // 处理下线的节点 可能会在CHANNEL_CACHE 不在address
            for (Map.Entry<InetSocketAddress,Channel> entry: RpcBootstrap.CHANNEL_CACHE.entrySet()){
                if(!addresses.contains(entry.getKey())){
                    RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            // 获得负载均衡器，进行重新的loadBalance
            LoadBalancer loadBalancer = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName,addresses);
        }
    }
    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
