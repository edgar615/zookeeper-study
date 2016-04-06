package com.edgar.curator.cache;

import com.google.common.collect.Lists;

import com.edgar.curator.service2.ExampleServer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Edgar on 2016/4/6.
 *
 * @author Edgar  Date 2016/4/6
 */
public class PathCacheExample {
//  可以利用ZooKeeper在集群的各个节点之间缓存数据。 每个节点都可以得到最新的缓存的数据。
// Curator提供了三种类型的缓存方式：Path Cache,Node Cache 和Tree Cache。

//  Path Cache用来监控一个ZNode的子节点. 当一个子节点增加， 更新，删除时， Path Cache会改变它的状态， 会包含最新的子节点， 子节点的数据和状态。 这也正如它的名字表示的那样， 那监控path。
//
//  实际使用时会涉及到四个类：
//
//  PathChildrenCache
//          PathChildrenCacheEvent
//  PathChildrenCacheListener
//          ChildData
//  想使用cache，必须调用它的start方法，不用之后调用close方法。 start有两个， 其中一个可以传入StartMode，用来为初始的cache设置暖场方式(warm)：
//
//  NORMAL: 初始时为空。
//  BUILD_INITIAL_CACHE: 在这个方法返回之前调用rebuild()。
//  POST_INITIALIZED_EVENT: 当Cache初始化数据后发送一个PathChildrenCacheEvent.Type#INITIALIZED事件
private static final String PATH = "/example/cache";

  public static void main(String[] args) throws Exception {
    TestingServer server = new TestingServer();
    CuratorFramework client = null;
    PathChildrenCache cache = null;
    try {
      client = CuratorFrameworkFactory
              .newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
      client.start();

      // in this example we will cache data. Notice that this is optional.
      cache = new PathChildrenCache(client, PATH, true);
      cache.start();

      processCommands(client, cache);
    } finally {
      CloseableUtils.closeQuietly(cache);
      CloseableUtils.closeQuietly(client);
      CloseableUtils.closeQuietly(server);
    }
  }

  private static void addListener(PathChildrenCache cache) {
    // a PathChildrenCacheListener is optional. Here, it's used just to log changes
    PathChildrenCacheListener listener = new PathChildrenCacheListener() {
      @Override
      public void childEvent(CuratorFramework client,
                             PathChildrenCacheEvent event) throws Exception {
        switch (event.getType()) {
          case CHILD_ADDED: {
            System.out.println("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
            break;
          }

          case CHILD_UPDATED: {
            System.out
                    .println("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
            break;
          }

          case CHILD_REMOVED: {
            System.out
                    .println("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
            break;
          }
        }
      }
    };
    cache.getListenable().addListener(listener);
  }

  private static void processCommands(CuratorFramework client,
                                      PathChildrenCache cache) throws Exception {
    // More scaffolding that does a simple command line processor

    printHelp();

    List<ExampleServer> servers = Lists.newArrayList();
    try {
      addListener(cache);

      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      boolean done = false;
      while (!done) {
        System.out.print("> ");

        String line = in.readLine();
        if (line == null) {
          break;
        }

        String command = line.trim();
        String[] parts = command.split("\\s");
        if (parts.length == 0) {
          continue;
        }
        String operation = parts[0];
        String args[] = Arrays.copyOfRange(parts, 1, parts.length);

        if (operation.equalsIgnoreCase("help") || operation.equalsIgnoreCase("?")) {
          printHelp();
        } else if (operation.equalsIgnoreCase("q") || operation.equalsIgnoreCase("quit")) {
          done = true;
        } else if (operation.equals("set")) {
          setValue(client, command, args);
        } else if (operation.equals("remove")) {
          remove(client, command, args);
        } else if (operation.equals("list")) {
          list(cache);
        }

        Thread.sleep(1000); // just to allow the console output to catch up
      }
    } finally {
      for (ExampleServer server : servers) {
        CloseableUtils.closeQuietly(server);
      }
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

  private static void remove(CuratorFramework client, String command,
                             String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("syntax error (expected remove <path>): " + command);
      return;
    }

    String name = args[0];
    if (name.contains("/")) {
      System.err.println("Invalid node name" + name);
      return;
    }
    String path = ZKPaths.makePath(PATH, name);

    try {
      client.delete().forPath(path);
    } catch (KeeperException.NoNodeException e) {
      // ignore
    }
  }

  private static void setValue(CuratorFramework client, String command,
                               String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("syntax error (expected set <path> <value>): " + command);
      return;
    }

    String name = args[0];
    if (name.contains("/")) {
      System.err.println("Invalid node name" + name);
      return;
    }
    String path = ZKPaths.makePath(PATH, name);

    byte[] bytes = args[1].getBytes();
    try {
      client.setData().forPath(path, bytes);
    } catch (KeeperException.NoNodeException e) {
      client.create().creatingParentContainersIfNeeded().forPath(path, bytes);
    }
  }

  private static void printHelp() {
    System.out.println(
            "An example of using PathChildrenCache. This example is driven by entering commands "
            + "at the prompt:\n");
    System.out.println("set <name> <value>: Adds or updates a node with the given name");
    System.out.println("remove <name>: Deletes the node with the given name");
    System.out.println("list: List the nodes/values in the cache");
    System.out.println("quit: Quit the example");
    System.out.println();
  }
}
