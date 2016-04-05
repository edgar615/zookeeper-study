package com.edgar.curator.crud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class WatchedGetClildrenExample {
  public static void main(String[] args) throws Exception {
    CuratorFramework client =
            CuratorFrameworkFactory.newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();
    String path = "/crud";
    if (client.checkExists().forPath(path) == null) {
      client.create().forPath(path);
    }

    CuratorListener curatorListener = new CuratorListener() {
      @Override
      public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
        System.out.println(event.getType() + ": " + event.getPath());
      }
    };
    client.getCuratorListenable().addListener(curatorListener);

    List<String> children = client.getChildren().watched().forPath(path);
    System.out.println(children);

    TimeUnit.SECONDS.sleep(3);

    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
            .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
            .forPath(path + "/" + new Random().nextInt(), "hoho".getBytes());;

    TimeUnit.SECONDS.sleep(3);
    client.close();
  }
}
