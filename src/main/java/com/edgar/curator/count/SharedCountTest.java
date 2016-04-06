package com.edgar.curator.count;

import com.google.common.collect.Lists;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/6.
 *
 * @author Edgar  Date 2016/4/6
 */
public class SharedCountTest {
  //  SharedCount int计数
//  SharedCount代表计数器， 可以为它增加一个SharedCountListener，当计数器改变时此Listener可以监听到改变的事件，而SharedCountReader
// 可以读取到最新的值， 包括字面值和带版本信息的值VersionedValue。
  private static final String PATH = "/example/share_count";

  public static void main(String[] args) throws Exception {
    CuratorFramework client = null;
    try {
      client = CuratorFrameworkFactory
              .newClient("10.4.7.48:2181", new ExponentialBackoffRetry(1000, 3));
      client.start();

      //任意的SharedCount， 只要使用相同的path，都可以得到这个计数值。
      SharedCount sharedCount = new SharedCount(client, PATH, 0);
      sharedCount.start();
      sharedCount.addListener(new SharedCountListener() {
        @Override
        public void countHasChanged(SharedCountReader sharedCount, int newCount) throws Exception {
          System.out.println("Counter's value is changed to " + newCount);
        }

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
          System.out.println("State changed: " + newState);
        }
      });

      ExecutorService service = Executors.newFixedThreadPool(10);
      List<SharedCount> examples = Lists.newArrayList();
      for (int i = 0; i < 10; i ++) {
        SharedCount count = new SharedCount(client, PATH, 0);
        examples.add(count);
        service.execute(new Runnable() {
          @Override
          public void run() {
            try {
              count.start();
              Thread.sleep(new Random().nextInt(10000));
              System.out.println("Increment:" + count.trySetCount(count.getVersionedValue(), count.getCount() + new Random().nextInt(10)));
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
      }
      service.shutdown();
      service.awaitTermination(10, TimeUnit.SECONDS);

      for (int i = 0; i < 10; i ++) {
        examples.get(i).close();
      }

      System.out.println(sharedCount.getCount());
      sharedCount.close();
    } finally {
      CloseableUtils.closeQuietly(client);
    }
  }


}
