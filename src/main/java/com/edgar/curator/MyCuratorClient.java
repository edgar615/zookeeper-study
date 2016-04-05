package com.edgar.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;

public class MyCuratorClient {

    public static void main(String[] args) throws Exception {
        //Getting a Connection
        CuratorFramework client = CuratorFrameworkFactory
                .newClient("10.4.7.48:2181", new RetryOneTime(1000));
        client.start();

      if (client.checkExists().forPath("/my/path") != null) {
        client.delete().deletingChildrenIfNeeded().forPath("/my");
      }
        //Calling ZooKeeper Directly
        client.create().creatingParentsIfNeeded().forPath("/my/path", "Hello, curator!".getBytes());

        client.delete().inBackground().forPath("/head");
        client.create().forPath("/head", new byte[0]);
        client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/head/child", new byte[0]);
        client.getData().watched().inBackground().forPath("/test");

      client.close();
    }
}