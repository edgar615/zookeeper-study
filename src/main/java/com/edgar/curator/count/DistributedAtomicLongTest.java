package com.edgar.curator.count;

import com.google.common.collect.Lists;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;

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
public class DistributedAtomicLongTest {
  //DistributedAtomicLong 除了计数的范围比SharedCount大了之外， 它首先尝试使用乐观锁的方式设置计数器， 如果不成功
  // (比如期间计数器已经被其它client更新了)， 它使用InterProcessMutex方式来更新计数值。


//  get(): 获取当前值
//  increment()： 加一
//  decrement(): 减一
//   add()： 增加特定的值
//    subtract(): 减去特定的值
//    trySet(): 尝试设置计数值
//     forceSet(): 强制设置计数值
//
//   你必须检查返回结果的succeeded()， 它代表此操作是否成功。 如果操作成功， preValue()代表操作前的值， postValue()代表操作后的值。

  private static final String PATH = "/example/distributed_long";

  public static void main(String[] args) throws Exception {
    CuratorFramework client = null;
    try {
      client = CuratorFrameworkFactory
              .newClient("10.4.7.48:2181", new ExponentialBackoffRetry(1000, 3));
      client.start();

      ExecutorService service = Executors.newFixedThreadPool(10);
      List<DistributedAtomicLong> examples = Lists.newArrayList();
      for (int i = 0; i < 10; i ++) {
        DistributedAtomicLong count = new DistributedAtomicLong(client, PATH, new
                RetryNTimes(5, 100));
        service.execute(new Runnable() {
          @Override
          public void run() {
            try {
              //Thread.sleep(rand.nextInt(1000));
              AtomicValue<Long> value = count.trySet(new Random().nextLong());
              //AtomicValue<Long> value = count.decrement();
              //AtomicValue<Long> value = count.add((long)rand.nextInt(20));
              System.out.println("succeed: " + value.succeeded());
              if (value.succeeded())
                System.out.println("Increment: from " + value.preValue() + " to " + value.postValue());
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
      }
      service.shutdown();
      service.awaitTermination(10, TimeUnit.SECONDS);

    } finally {
      CloseableUtils.closeQuietly(client);
    }
  }
}
