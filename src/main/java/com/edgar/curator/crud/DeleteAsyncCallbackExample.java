package com.edgar.curator.crud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
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
public class DeleteAsyncCallbackExample {
  public static void main(String[] args) throws Exception {
    CuratorFramework client =
            CuratorFrameworkFactory.newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();
    String path = "/crud/delete";
    if (client.checkExists().forPath(path) != null) {
      client.delete().forPath(path);
    }
    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
            .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
            .forPath(path, "hoho".getBytes());
    System.out.println(client.checkExists().forPath(path));

    client.delete().deletingChildrenIfNeeded().inBackground(new BackgroundCallback() {
      @Override
      public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
        System.out.println(event.getType() + ": " + event.getPath());
      }
    }).forPath(path);
    System.out.println(client.checkExists().forPath(path));
    TimeUnit.SECONDS.sleep(5);
    client.close();
  }
}
