package com.edgar.curator.leader.latch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.RetryOneTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class LeaderLatchExample {
  public static void main(String[] args) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory
            .newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();

    String path = "/_leader_latch";

    List<LeaderLatchClient> clients = new ArrayList<>();

    for (int i = 0; i < 10; i ++) {
      String name = "Client-" + i;
      LeaderLatchClient leaderLatchClient = new LeaderLatchClient(client, path, name);
      leaderLatchClient.start();
      clients.add(leaderLatchClient);
    }
    TimeUnit.SECONDS.sleep(10);

    LeaderLatchClient leader = null;
    for (int i = 0; i < 10; i ++) {
      System.out.println("Client-" + i);
      clients.get(i).dispaly();
      if (clients.get(i).isLeader()) {
        leader = clients.get(i);
      }
    }

    leader.close();

    TimeUnit.SECONDS.sleep(10);

    for (int i = 0; i < 10; i ++) {
      System.out.println("Client-" + i);
      clients.get(i).dispaly();
      if (clients.get(i).isLeader()) {
        leader = clients.get(i);
      }
    }
  }
}
