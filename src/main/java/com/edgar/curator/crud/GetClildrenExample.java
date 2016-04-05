package com.edgar.curator.crud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.List;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class GetClildrenExample {
  public static void main(String[] args) throws Exception {
    CuratorFramework client =
            CuratorFrameworkFactory.newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();
    String path = "/crud";
    if (client.checkExists().forPath(path) == null) {
      client.create().forPath(path);
    }
    List<String> children = client.getChildren().forPath(path);
    System.out.println(children);
    client.close();
  }
}
