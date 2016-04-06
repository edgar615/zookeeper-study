package com.edgar.curator.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/6.
 *
 * @author Edgar  Date 2016/4/6
 */
public class TreeCacheTest {
  private static final String PATH = "/example/cache";

  public static void main(String[] args) throws Exception {

    CuratorFramework client = null;
    TreeCache cache = null;
    try {
      client = CuratorFrameworkFactory
              .newClient("10.4.7.48:2181", new ExponentialBackoffRetry(1000, 3));
      client.start();

      cache = new TreeCache(client, PATH);
      cache.start();

      final TreeCache finalCache = cache;
      TreeCacheListener listener = new TreeCacheListener() {

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
          switch (event.getType()) {
            case NODE_ADDED: {
              System.out.println("TreeNode added: " + ZKPaths
                      .getNodeFromPath(event.getData().getPath()) + ", value: "
                                 + new String(event.getData().getData()));
              break;
            }
            case NODE_UPDATED: {
              System.out.println(
                      "TreeNode changed: " + ZKPaths.getNodeFromPath(event.getData().getPath())
                      + ", value: "
                      + new String(event.getData().getData()));
              break;
            }
            case NODE_REMOVED: {
              System.out.println(
                      "TreeNode removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
              break;
            }
            default:
              System.out.println("Other event: " + event.getType().name());
          }
        }

      };

      cache.getListenable().addListener(listener);

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

  private static void list(TreeCache cache) {
    if (cache.getCurrentChildren(PATH).size() == 0) {
      System.out.println("* empty *");
    } else {
      for (Map.Entry<String, ChildData> entry : cache.getCurrentChildren(PATH).entrySet()) {
        System.out.println(entry.getKey() + " = " + new String(entry.getValue().getData()));
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
