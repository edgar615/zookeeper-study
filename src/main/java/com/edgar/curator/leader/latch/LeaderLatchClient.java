package com.edgar.curator.leader.latch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.RetryOneTime;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class LeaderLatchClient {

  private final String name;

  private final LeaderLatch leaderLatch;

  public LeaderLatchClient(CuratorFramework client, String path, String name) {
    this.name = name;

    //    client - the client
//    latchPath - the path for this leadership group
//    id - participant ID
    leaderLatch = new LeaderLatch(client, path, name);
//    leaderLatch.addListener();
  }

  public void start() throws Exception {
    leaderLatch.start();
  }

  public void close() throws Exception {
    leaderLatch.close();
  }


  public void dispaly() throws Exception {
    System.out.println(leaderLatch.hasLeadership());
    System.out.println(leaderLatch.getParticipants());
  }

  public static void main(String[] args) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory
            .newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();

    String path = "/_leader_latch";
//    client - the client
//    latchPath - the path for this leadership group
//    id - participant ID
    LeaderLatch leaderLatch = new LeaderLatch(client, path);
    leaderLatch.start();
    //一旦启动， LeaderLatch会和其它使用相同latch path的其它LeaderLatch交涉，然后随机的选择其中一个作为leader。
    // 你可以随时查看一个给定的实例是否是leader:
    TimeUnit.SECONDS.sleep(10);
    System.out.println(leaderLatch.hasLeadership());
    System.out.println(leaderLatch.getParticipants());

//    异常处理
//    LeaderLatch实例可以增加ConnectionStateListener来监听网络连接问题。 当 SUSPENDED 或 LOST 时,
// leader不再认为自己还是leader.当LOST 连接重连后 RECONNECTED,LeaderLatch会删除先前的ZNode然后重新创建一个.
//    LeaderLatch用户必须考虑导致leadershi丢失的连接问题。 强烈推荐你使用ConnectionStateListener。

//    一旦不使用LeaderLatch了，必须调用close方法。 如果它是leader,会释放leadership， 其它的参与者将会选举一个leader。
    leaderLatch.close();
  }

  public boolean isLeader() {
    return leaderLatch.hasLeadership();
  }

  public String getName() {
    return name;
  }

  public LeaderLatch getLeaderLatch() {
    return leaderLatch;
  }
}
