package com.edgar.curator.crud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class CreateExample {
  public static void main(String[] args) throws Exception {
    CuratorFramework client =
            CuratorFrameworkFactory.newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();
    String path = "/crud/create";
    if (client.checkExists().forPath(path) != null) {
      client.delete().forPath(path);
    }
    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
            .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
            .forPath(path, "hoho".getBytes());
    client.close();
  }
}
