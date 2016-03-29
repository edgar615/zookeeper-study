package com.edgar.zookeeper.servicediscovery;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

/**
 * Created by Edgar on 2016/3/29.
 *
 * @author Edgar  Date 2016/3/29
 */
public class ServiceDiscovery implements Runnable {
  private static final String BASE_PATH = "/services";

  private final String zkConnect;

  private ZooKeeper zk;

  boolean alive = true;

  public ServiceDiscovery(String zkConnect) {
    this.zkConnect = zkConnect;
    Watcher childrenWatcher = new Watcher() {
      @Override
      public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
          try {
            List<String> children = zk.getChildren(BASE_PATH, this);
            System.err.println("Members: " + children);
          } catch (KeeperException | InterruptedException e) {
            alive = false;
            e.printStackTrace();
          }
        }
      }
    };
    try {
      zk = new ZooKeeper(zkConnect, 2000, null);
    } catch (IOException e) {
      e.printStackTrace();
      alive = false;
    }
    if (zk != null) {
      try {
        if (zk.exists(BASE_PATH, false) == null) {
          zk.create(BASE_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        List<String> children = zk.getChildren(BASE_PATH, childrenWatcher);
        System.out.println("Members: " + children);
      } catch (KeeperException | InterruptedException e) {
        alive = false;
        e.printStackTrace();
      }
    }
  }

  @Override
  public void run() {
    try {
      synchronized (this) {
        while (alive) {
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

  public static void main(String[] args) {
    new ServiceDiscovery("10.4.7.48:2181").run();
  }
}
