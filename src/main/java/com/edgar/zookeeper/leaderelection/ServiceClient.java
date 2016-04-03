package com.edgar.zookeeper.leaderelection;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by edgar on 16-4-3.
 */
public class ServiceClient implements Runnable, Watcher {

  public static void main(String[] args) {
    new ServiceClient("localhost:2181").run();
  }

  private static final String BASE_PATH = "/election";

  private final String zkConnect;

  private ZooKeeper zk;

  private String path;

  private String prePath;

  private boolean isLeader = false;

  public ServiceClient(String zkConnect) {
    this.zkConnect = zkConnect;
    try {
      zk = new ZooKeeper(zkConnect, 2000, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (zk != null) {
      try {
        if (zk.exists(BASE_PATH, false) == null) {
          zk.create(BASE_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        String node = zk.create(BASE_PATH + "/foo", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        this.path = node.substring(10);
        System.out.println("node:" + path);
      } catch (KeeperException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      try {
        List<String> nodes = zk.getChildren(BASE_PATH, null);
        Collections.sort(nodes);
        System.out.println(nodes);
        if (nodes.size() == 1) {
          isLeader = true;
          System.out.println(path + " is leader");
        } else {
          System.out.println(path + " is follower");
          prePath = nodes.get(nodes.indexOf(path) - 1);
          System.out.println("watcher : " + prePath);
          zk.exists(BASE_PATH + "/" + prePath, this);
        }

      } catch (KeeperException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void run() {
    try {
      synchronized (this) {
        while (true) {
          wait();
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    } finally {
      this.close();
    }
  }

  public synchronized void close() {
    try {
      zk.close();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  @Override
  public void process(WatchedEvent event) {
    System.out.println(prePath + "down");
    try {
      List<String> nodes = zk.getChildren(BASE_PATH, null);
      Collections.sort(nodes);
      System.out.println(nodes);
      String firstNode = nodes.get(0);
      if (firstNode.equals(path)) {
        System.out.println(path + " become a leader");
      } else {
        prePath = nodes.get(nodes.indexOf(path) - 1);
        System.out.println("watcher : " + prePath);
        zk.exists(BASE_PATH + "/" + prePath, this);
      }

    } catch (KeeperException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
