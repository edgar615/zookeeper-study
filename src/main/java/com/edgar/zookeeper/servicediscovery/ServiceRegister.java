package com.edgar.zookeeper.servicediscovery;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Edgar on 2016/3/29.
 *
 * @author Edgar  Date 2016/3/29
 */
public class ServiceRegister implements Runnable {

  private static final String BASE_PATH = "/services";

  private final String zkConnect;

  private final String serviceId;

  private ZooKeeper zk;

  public ServiceRegister(String serviceId, String zkConnect) {
    this.serviceId = serviceId;
    this.zkConnect = zkConnect;
    try {
      zk = new ZooKeeper(zkConnect, 2000, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (zk != null) {
      try {
        zk.create(BASE_PATH + "/" + serviceId, serviceId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                  CreateMode.EPHEMERAL);
      } catch (KeeperException | InterruptedException e) {
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

  public static void main(String[] args) {
    new ServiceRegister(UUID.randomUUID().toString(), "10.4.7.48:2181").run();
  }

}
