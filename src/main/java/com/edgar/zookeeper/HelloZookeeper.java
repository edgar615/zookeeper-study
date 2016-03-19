package com.edgar.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

/**
 * Created by edgar on 16-3-18.
 */
public class HelloZookeeper {
  public static void main(String[] args) throws IOException {
    String hostPort = "localhost:2181";
    String zkPath = "/";
    //connectString : This is a comma-separated list of host:port pairs, each of which corresponds to a ZooKeeper server.
    // For example, 10.0.0.1:2001 ,10.0.0.2:2002 , and 10.0.0.3:2003 represent a valid host:port pair for ZooKeeper ensemble of three nodes.
//    sessionTimeout : This is the session timeout in milliseconds. This is the
//    amount of time ZooKeeper waits without getting a heartbeat from the client
//    before declaring the session as dead.
    //watcher : A watcher object, which, if created, will be notified of state changes
//    and node events. This watcher object needs to be created separately through
//    a user-defined class by implementing the Watcher interface and passing the
//            instantiated object to the ZooKeeper constructor. A client application can
//    get a notification for various types of events such as connection loss, session
//    expiry, and so on.

//    •	 sessionId : In case the client is reconnecting to the ZooKeeper server, a
//    specific session ID can be used to refer to the previously connected session
//    •	 sessionPasswd : If the specified session requires a password, this can be
//    specified here
    ZooKeeper zk = new ZooKeeper(hostPort, 2000, null);
    if (zk != null) {
      try {
        List<String> zooChildren = zk.getChildren(zkPath, false);
        System.out.println("Znodes of '/': ");
        for (String child : zooChildren) {
          System.out.println(child);
        }
      } catch (KeeperException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
