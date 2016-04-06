package com.edgar.curator.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/6.
 *
 * @author Edgar  Date 2016/4/6
 */
public class NodeCacheTest {
  private static final String PATH = "/example/node_cache";

  //Tree Cache这种类型的即可以监控节点的状态，还监控节点的子节点的状态， 类似上面两种cache的组合。 这也就是Tree的概念。 它监控整个树中节点的状态。 涉及到下面四个类。

  public static void main(String[] args) throws Exception {

    CuratorFramework client = null;
    NodeCache cache = null;
    try {
      client = CuratorFrameworkFactory
              .newClient("10.4.7.48:2181", new ExponentialBackoffRetry(1000, 3));
      client.start();

      cache = new NodeCache(client, PATH, false);
      cache.start();

      final NodeCache finalCache = cache;
      NodeCacheListener listener = new NodeCacheListener() {
        @Override
        public void nodeChanged() throws Exception {
          if (finalCache.getCurrentData() != null) {
            System.out.println("node changed" + finalCache.getCurrentData().getPath() + " = " +
                               new String(finalCache.getCurrentData().getData()));
          }
        }
      };

      cache.getListenable().addListener(listener);

//      设置/更新、移除其实是使用client (CuratorFramework)来操作, 不通过PathChildrenCache操作：
//
//      client.setData().forPath(path, bytes);
//      client.create().creatingParentsIfNeeded().forPath(path, bytes);
//      client.delete().forPath(path);

      //add cache
      setValue(client, "foo");
      setValue(client, "hello");
      TimeUnit.SECONDS.sleep(3);

      //list cache
      list(cache);

      //remove data
      remove(client);
      //update
      setValue(client, "foo");

      list(cache);

    } finally {
      CloseableUtils.closeQuietly(cache);
      CloseableUtils.closeQuietly(client);
    }
  }

  private static void list(NodeCache cache) {
    if (cache.getCurrentData() == null) {
      System.out.println("* empty *");
    } else {
      System.out.println(cache.getCurrentData().getPath() + " = " + new String(
              cache.getCurrentData().getData()));
    }
  }

  private static void setValue(CuratorFramework client, String value) throws Exception {

    byte[] bytes = value.getBytes();
    try {
      client.setData().forPath(PATH, bytes);
    } catch (KeeperException.NoNodeException e) {
      client.create().creatingParentContainersIfNeeded().forPath(PATH, bytes);
    }
  }

  private static void remove(CuratorFramework client) throws Exception {

    client.delete().forPath(PATH);
  }
}
