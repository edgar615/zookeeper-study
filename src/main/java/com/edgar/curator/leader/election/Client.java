package com.edgar.curator.leader.election;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2016/4/6.
 *
 * @author Edgar  Date 2016/4/6
 */
public class Client extends LeaderSelectorListenerAdapter implements Closeable {
  private final String name;

  private LeaderSelector leaderSelector;

  //获取领导权的次数
  private final AtomicInteger leaderCount = new AtomicInteger();

  public Client(CuratorFramework client, String path, String name) {
    this.name = name;
    this.leaderSelector = new LeaderSelector(client, path, this);
    // for most cases you will want your instance to requeue when it relinquishes leadership
    //保证在此实例释放领导权之后还可能获得领导权。
    leaderSelector.autoRequeue();
  }

  //一旦启动，当实例取得领导权时你的listener的takeLeadership()方法被调用. 而takeLeadership()方法只有领导权被释放时才返回。
//  异常处理
//  LeaderSelectorListener类继承ConnectionStateListener.LeaderSelector必须小心连接状态的改变. 如果实例成为leader,
// 它应该相应SUSPENDED 或 LOST. 当 SUSPENDED 状态出现时， 实例必须假定在重新连接成功之前它可能不再是leader了。 如果LOST状态出现，
// 实例不再是leader， takeLeadership方法返回.
//
//          重要: 推荐处理方式是当收到SUSPENDED 或 LOST时抛出CancelLeadershipException异常.
// 这会导致LeaderSelector实例中断并取消执行takeLeadership方法的异常. 这非常重要， 你必须考虑扩展LeaderSelectorListenerAdapter.
// LeaderSelectorListenerAdapter提供了推荐的处理逻辑。
  @Override
  public void takeLeadership(CuratorFramework client) throws Exception {
    // we are now the leader. This method should not return until we want to relinquish leadership

    final int         waitSeconds = (int)(5 * Math.random()) + 1;

    System.out.println(name + " is now the leader. Waiting " + waitSeconds + " seconds...");
    System.out.println(name + " has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
    try
    {
      Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
    }
    catch ( InterruptedException e )
    {
      System.err.println(name + " was interrupted.");
      Thread.currentThread().interrupt();
    }
    finally
    {
      System.out.println(name + " relinquishing leadership.\n");
    }
  }


  public void start() throws IOException
  {
    // the selection for this instance doesn't start until the leader selector is started
    // leader selection is done in the background so this call to leaderSelector.start() returns immediately
    leaderSelector.start();
  }


  //当你不再使用LeaderSelector实例时，应该调用它的close方法。
  @Override
  public void close() throws IOException {
    leaderSelector.close();
  }
}
