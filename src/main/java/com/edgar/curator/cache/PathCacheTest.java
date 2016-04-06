package com.edgar.curator.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/6.
 *
 * @author Edgar  Date 2016/4/6
 */
public class PathCacheTest {
  private static final String PATH = "/example/cache";

  public static void main(String[] args) throws Exception {

    CuratorFramework client = null;
    PathChildrenCache cache = null;
    try {
      client = CuratorFrameworkFactory
              .newClient("10.4.7.48:2181", new ExponentialBackoffRetry(1000, 3));
      client.start();

      // in this example we will cache data. Notice that this is optional.
      cache = new PathChildrenCache(client, PATH, true);
      cache.start();

      PathChildrenCacheListener listener = new PathChildrenCacheListener() {
        @Override
        public void childEvent(CuratorFramework client,
                               PathChildrenCacheEvent event) throws Exception {
          switch (event.getType()) {
            case CHILD_ADDED: {
              System.out
                      .println("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
              break;
            }

            case CHILD_UPDATED: {
              System.out
                      .println("Node changed: " + ZKPaths
                              .getNodeFromPath(event.getData().getPath()));
              break;
            }

            case CHILD_REMOVED: {
              System.out
                      .println("Node removed: " + ZKPaths
                              .getNodeFromPath(event.getData().getPath()));
              break;
            }
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
      setValue(client, "foo", "bar");
      setValue(client, "hello", "world");
      TimeUnit.SECONDS.sleep(3);

      //list cache
      list(cache);

      //remove data
      remove(client, "hello");
      //update
      setValue(client, "foo", "hoho");

      list(cache);

    } finally {
      CloseableUtils.closeQuietly(cache);
      CloseableUtils.closeQuietly(client);
    }
  }

  private static void list(PathChildrenCache cache) {
    if (cache.getCurrentData().size() == 0) {
      System.out.println("* empty *");
    } else {
      for (ChildData data : cache.getCurrentData()) {
        System.out.println(data.getPath() + " = " + new String(data.getData()));
      }
    }
  }

  private static void setValue(CuratorFramework client, String key, String value) throws Exception {

    if (key.contains("/")) {
      System.err.println("Invalid node name" + key);
      return;
    }
    String path = ZKPaths.makePath(PATH, key);

    byte[] bytes = value.getBytes();
    try {
      client.setData().forPath(path, bytes);
    } catch (KeeperException.NoNodeException e) {
      client.create().creatingParentContainersIfNeeded().forPath(path, bytes);
    }
  }

  private static void remove(CuratorFramework client, String key) throws Exception {

    if (key.contains("/")) {
      System.err.println("Invalid node name" + key);
      return;
    }
    String path = ZKPaths.makePath(PATH, key);

    try {
      client.delete().forPath(path);
    } catch (KeeperException.NoNodeException e) {
      // ignore
    }
  }
}
