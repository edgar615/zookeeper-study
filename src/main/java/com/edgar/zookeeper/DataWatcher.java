package com.edgar.zookeeper;

import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Created by edgar on 16-3-18.
 */
public class DataWatcher implements Watcher, Runnable {
  private static String hostPort = "localhost:2181";
  private static String zooDataPath = "/MyConfig";
  byte[] zoo_data = null;
  ZooKeeper zk;

  public DataWatcher() {
    try {
      zk = new ZooKeeper(hostPort, 2000, this);
      if (zk != null) {
        try {
          if (zk.exists(zooDataPath, this) == null) {
            zk.create(zooDataPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
          }
        } catch (KeeperException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void printData() throws KeeperException, InterruptedException {
    zoo_data = zk.getData(zooDataPath, this, null);
    String zString = new String(zoo_data);
    System.out.printf("\nCurrent Data @ ZK Path %s: %s",
            zooDataPath, zString);
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
    }
  }

  @Override
  public void process(WatchedEvent event) {
    //注意，这里最好把异常捕获，不然出现异常没有日志
    System.out.printf(
            "\nEvent Received: %s", event.toString());
    if (event.getType() == Event.EventType.NodeDataChanged) {
      try {
        printData();
      } catch (KeeperException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws KeeperException, InterruptedException {
    DataWatcher dataWatcher = new DataWatcher();
    dataWatcher.printData();
    dataWatcher.run();
  }
}
