package com.edgar.zookeeper;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by edgar on 16-3-18.
 */
public class ClusterMonitor implements Runnable {
  private static String membershipRoot = "/Members";
  private final Watcher connectionWatcher;
  private final Watcher childrenWatcher;
  private ZooKeeper zk;
  boolean alive = true;

  public ClusterMonitor(String HostPort) throws IOException, KeeperException, InterruptedException {
    connectionWatcher = new Watcher() {
      @Override
      public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.None && event.getState() == Event.KeeperState.SyncConnected) {
          System.out.printf("\nEvent Received: %s",
                  event.toString());
        }
      }
    };
    childrenWatcher = new Watcher() {
      @Override
      public void process(WatchedEvent event) {
        System.out.printf("\nEvent Received: %s",
                event.toString());
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
          try {
            List<String> children = zk.getChildren(membershipRoot, this);
            wall("!!!Cluster Membership Change!!!");
            wall("Members: " + children);
          } catch (KeeperException e) {
            throw new RuntimeException(e);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            alive = false;
            throw new RuntimeException(e);
          }
        }
      }
    };
    zk = new ZooKeeper(HostPort, 2000, connectionWatcher);
    if (zk.exists(membershipRoot, false) == null) {
      zk.create(membershipRoot, "ClusterMonitorRoot".getBytes(),
              ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }
    List<String> children = zk.getChildren(membershipRoot, childrenWatcher);
    System.err.println("Members: " + children);
  }

  public synchronized void close() {
    try {
      zk.close();
    } catch (InterruptedException e) {
      e.printStackTrace();
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

  public void wall(String message) {
    System.out.printf("\nMESSAGE: %s", message);
  }

  public static void main(String[] args) throws
          IOException, InterruptedException, KeeperException {
    new ClusterMonitor("localhost:2181").run();
  }
}
