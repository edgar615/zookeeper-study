package com.edgar.curator.group;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.apache.curator.retry.RetryOneTime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/6.
 *
 * @author Edgar  Date 2016/4/6
 */
public class GroupClient {
  public static void main(String[] args) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory
            .newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();

    GroupMember member = new GroupMember(client, "/members", UUID.randomUUID().toString());
    member.start();
    TimeUnit.SECONDS.sleep(10);
    Map<String, byte[]> members = member.getCurrentMembers();
    members.forEach((key, value) -> System.out.println(key));
  }
}
