package com.edgar.curator.crud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class SetDataAsyncExample {
  public static void main(String[] args) throws Exception {
    CuratorFramework client =
            CuratorFrameworkFactory.newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();
    String path = "/crud/data";
    if (client.checkExists().forPath(path) != null) {
      client.delete().forPath(path);
    } else {
      client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path);
    }

    CuratorListener curatorListener = new CuratorListener() {
      @Override
      public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
        System.out.println(event.getType() + ": " + event.getPath());
      }
    };

    client.getCuratorListenable().addListener(curatorListener);
    client.setData().inBackground()
            .forPath(path, "hoho".getBytes());
    TimeUnit.SECONDS.sleep(5);
    client.close();
  }
}
