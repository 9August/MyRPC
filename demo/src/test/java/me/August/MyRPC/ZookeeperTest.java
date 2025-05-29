package me.August.MyRPC;

import me.August.MyRPC.zookeeper.MyWatcher;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


public class ZookeeperTest {
    ZooKeeper zooKeeper;

    @Before
    public void createZk() {
        // 定义连接参数
        String connectString = "127.0.0.1:2181";
        // 定义超时时间
        int timeout = 10000;
        try {
            zooKeeper = new ZooKeeper(connectString, timeout, new MyWatcher());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreatePNode() {
        try {
            String result = zooKeeper.create("/rpclass", "hello".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("result = " + result);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeletePNode() {
        try {
            // version: cas mysql 乐观锁， 也可以无视版本号 -1
            zooKeeper.delete("/rpclass", -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testExistsPNode() {
        try {
            // version: cas mysql 乐观锁， 也可以无视版本号 -1
            Stat stat = zooKeeper.exists("/rpclass", null);
            zooKeeper.setData("/rpclass", "hi".getBytes(), -1);
            // 当前节点的数据版本
            int version = stat.getVersion();
            System.out.println("version = " + version);
            // 当前节点的acl数据版本
            int aversion = stat.getAversion();
            System.out.println("aversion = " + aversion);
            // 当前子节点数据的版本
            int cversion = stat.getCversion();
            System.out.println("cversion = " + cversion);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void testWatcher() {
        try {
            // 以下三个方法可以注册watcher，可以直接new一个新的watcher，
            // 也可以使用true来选定默认的watcher
            System.out.println("999");
            zooKeeper.exists("/rpclass", true);
            System.out.println("12");
            // zooKeeper.getChildren();
            // zooKeeper.getData();
            while (true) {
                Thread.sleep(1000);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}



