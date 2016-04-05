package com.edgar.curator.crud;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class DeleteGuaranteedExample {

  /*
Guaranteed Delete

Solves this edge case: deleting a node can fail due to connection issues. Further,
if the node was
ephemeral, the node will not get auto-deleted as the session is still valid. This
can wreak havoc
with lock implementations.


When guaranteed is set, Curator will record failed node deletions and attempt to
delete them in the
background until successful. NOTE: you will still get an exception when the deletion
 fails. But, you
can be assured that as long as the CuratorFramework instance is open attempts will
be made to delete
the node.
*/
  public static void main(String[] args) throws Exception {
    CuratorFramework client =
            CuratorFrameworkFactory.newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();
    String path = "/crud/delete";
    if (client.checkExists().forPath(path) != null) {
      client.delete().forPath(path);
    }
    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
            .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
            .forPath(path, "hoho".getBytes());
    System.out.println(client.checkExists().forPath(path));
    client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
    System.out.println(client.checkExists().forPath(path));
    client.close();
  }
}
