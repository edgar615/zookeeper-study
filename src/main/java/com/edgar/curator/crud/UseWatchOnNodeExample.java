package com.edgar.curator.crud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class UseWatchOnNodeExample {
  public static void main(String[] args) throws Exception {
    CuratorFramework client =
            CuratorFrameworkFactory.newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();
    String path = "/crud/watcher";
    if (client.checkExists().forPath(path) != null) {
      client.delete().forPath(path);
    }

    Watcher watcher = new Watcher() {
      @Override
      public void process(WatchedEvent event) {
        System.out.println(event.getType() + ": " + event.getPath());
      }
    };
    client.checkExists().usingWatcher(watcher).forPath(path);

    TimeUnit.SECONDS.sleep(3);

    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
            .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
            .forPath(path, "hoho".getBytes());
    ;

    TimeUnit.SECONDS.sleep(3);
    client.close();
  }
}
