package com.edgar.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by edgar on 16-3-18.
 */
public class DataUpdater implements Watcher {
  private static String hostPort = "localhost:2181";
  private static String zooDataPath = "/MyConfig";
  private ZooKeeper zk;

  public DataUpdater() {
    try {
      this.zk = new ZooKeeper(hostPort, 2000, this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void process(WatchedEvent event) {
    System.out.printf("\nEvent Received: %s", event.toString());
  }

  public static void main(String[] args) throws KeeperException, InterruptedException {
    DataUpdater updater = new DataUpdater();
    updater.run();
  }

  private void run() throws KeeperException, InterruptedException {
    while (true) {
      String uuid = UUID.randomUUID().toString();
      byte[] zoo_data = uuid.getBytes();
      zk.setData(zooDataPath, zoo_data, -1);
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      }

    }
  }
}
