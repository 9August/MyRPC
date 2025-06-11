package me.August.MyRPC.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.ServiceConfig;
import me.August.MyRPC.constants.Constant;
import me.August.MyRPC.discovery.AbstractRegistry;
import me.August.MyRPC.exceptons.DiscoveryException;
import me.August.MyRPC.utils.NetUtils;
import me.August.MyRPC.utils.zookeeper.ZookeeperNode;
import me.August.MyRPC.utils.zookeeper.ZookeeperUtils;
import me.August.MyRPC.watch.UpAndDownWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;


@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    // 维护一个zk实例
    private ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public ZookeeperRegistry(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectString, timeout);
    }


    @Override
    public void register(ServiceConfig<?> service) {
        // 服务名称的节点
        String parentNode = Constant.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();
        // 建立服务节点这个节点应该是一个持久节点
        if (!ZookeeperUtils.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }

        // 建立分组节点
        parentNode = parentNode + "/" + service.getGroup();
        if(!ZookeeperUtils.exists(zooKeeper,parentNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode,null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }


        //todo: 后续处理端口的问题
        String node = parentNode + "/" + NetUtils.getIp() + ":" + RpcBootstrap.getInstance().getConfiguration().getPort();
        if (!ZookeeperUtils.exists(zooKeeper, node, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }

        if (log.isDebugEnabled()) {
            log.debug("服务{}，已经被注册", service.getInterface().getName());
        }
    }


    // 拉取合适的服务列表
    @Override
    public List<InetSocketAddress> lookup(String serviceName, String group) {
        // 1、找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDERS_PATH + "/" + serviceName ;
        //   + "/" + "group";

        // 2、从zk中获取他的子节点, 192.168.12.123:2151
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        // 获取了所有的可用的服务列表
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();

        if (inetSocketAddresses.isEmpty()) {
            throw new DiscoveryException("未发现任何可用的服务主机.");
        }

        return inetSocketAddresses;
    }

}
