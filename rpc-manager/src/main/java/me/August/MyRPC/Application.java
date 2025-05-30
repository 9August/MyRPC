package me.August.MyRPC;

import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.utils.zookeeper.ZookeeperNode;
import me.August.MyRPC.utils.zookeeper.ZookeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

@Slf4j
public class Application {
    public static void main(String[] args) {
        // 创建基础目录
        // 创建一个zookeeper实例
        ZooKeeper zooKeeper = ZookeeperUtils.createZookeeper();

        // 定义节点和数据
        String basePath = "/rpc-metadata";
        String providerPath = basePath + "/providers";
        String consumersPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providersNode = new ZookeeperNode(providerPath, null);
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);
        // 创建节点
        List.of(baseNode, providersNode, consumersNode).forEach(node -> {
            ZookeeperUtils.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        });
        // 关闭连接
        ZookeeperUtils.close(zooKeeper);
    }
}
