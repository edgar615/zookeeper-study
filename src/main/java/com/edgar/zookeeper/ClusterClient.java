package com.edgar.zookeeper;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Created by edgar on 16-3-18.
 */
public class ClusterClient implements Watcher, Runnable {
  private static String membershipRoot = "/Members";
  private ZooKeeper zk;
  public ClusterClient(String hostPort, Long pid) {
    String proccessId = pid.toString();
    try {
      zk = new ZooKeeper(hostPort, 2000, this);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (zk != null) {
      try {
        zk.create(membershipRoot + "/" + proccessId, proccessId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
      } catch (KeeperException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void process(WatchedEvent event) {
    System.out.printf("\nEvent Received: %s", event.toString());
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

  private synchronized void close() {
    try {
      zk.close();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
//Get the process id
    String name = ManagementFactory.getRuntimeMXBean().getName();
    int index = name.indexOf('@');
    Long processId = Long.parseLong(name.substring(0, index));
    new ClusterClient("localhost:2181", processId).run();
  }
}
