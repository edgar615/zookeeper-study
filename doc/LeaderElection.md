A leader election algorithm has the following two required properties:
•	 Liveness: This ensures that most of the time, there is a leader
•	 Safety: This ensures that at any given time, there is either no leader
or one leader

Client processes nominating themselves as leaders use the SEQUENCE | EPHEMERAL
flags when creating znodes under a parent znode. ZooKeeper automatically appends
a monotonically increasing sequence number as a suffix to the child znode as the
sequence flag is set. The process that created the znode with the smallest appended
sequence number is elected as the leader. However, the algorithm should also take
into account the failure of the leader.

Let /_election_ be the election znode path that acts as the root for all clients
participating in the leader election algorithm.
Clients with proposals for their nomination in the leader election procedure perform
the following steps:

1.	 Create a znode with the /_election_/candidate-sessionID_ path, with
both the SEQUENCE and EPHEMERAL flags. The sessionID identifier, as a part
of the znode name, helps in recognizing znodes in the case of partial failures
due to connection loss. Now, say that ZooKeeper assigns a sequence number
N to the znode when the create() call succeeds.
2.	 Retrieve the current list of children in the election znode as follows:
L = getChildren("/_election_", false)
Here, L represents the list of children of "/_election_" .
The watch is set to false to prevent any herd effect.
3.	 Set a watch for changes in /_election_/candidate-sessionID_M , where M
is the largest sequence number such that M is less than N, and candidate-
sessionID_M is a znode in L as follows:
exists("/_election_/candidate-sessionID_M", true)
4.	 Upon receiving a notification of znode deletion for the watches set in
step 3, execute the getChildren(("/_election_", false) method
on the election znode.
5.	 Let L be the new list of children of _election_ . The leader is then elected
as follows:
1.	 If candidate-sessionID_N (this client) is the smallest node in L , then
declare itself as the leader.
2.	 Watch for changes on /_election_/candidate-sessionID_M ,
where M is the largest sequence number such that M is less than N
and candidate-sessionID_M is a znode in L .
6.	 If the current leader crashes, the client having the znode with the next
highest sequence number becomes the leader and so on.

ZooKeeper 需要在所有的服务（可以理解为服务器）中选举出一个 Leader ，然后让这个 Leader 来负责管理集群。此时，集群中的其它服务器则成为此 Leader 的 Follower 。
并且，当 Leader 故障的时候，需要 ZooKeeper 能够快速地在 Follower 中选举出下一个 Leader 。

算法
1. 每个服务创建一个/_election_/candidate-sessionID_的节点，节点的类型是SEQUENCE| EPHEMERAL类型。
在 SEQUENCE 标志下， ZooKeeper 将自动地为每一个 ZooKeeper 服务器分配一个比前一个分配的序号要大的序号。
序列号最小的节点将成为Leader

2.每个服务读取/_election_/节点下的子节点,L = getChildren("/_election_", false),这里并不监听子结点的变化,是为了避免从众效应.

3.每个服务监听/_election_/candidate-sessionID_M节点的编号，M是比自己节点的序列号小一号的节点,exists("/_election_/candidate-sessionID_M", true)。

4.当服务监听到/_election_/candidate-sessionID_M的删除事件之后，读取/_election_/节点下的子节点getChildren(("/_election_", false)

5.在读取到/_election_节点下的子节点挂掉后，按照下面的算法选举出一个leader:

1) 如果服务的/_election_/candidate-sessionID_N,是最小的节点，把它选举为leader
2) 监听比自己节点的序列号小一号的节点

6.如果leader挂了，监听了这个leader的服务会选举为新的leader。

此操作实现的核心思想是：首先创建一个 EPHEMERAL 目录节点，例如“ /election ”。然后。每一个 ZooKeeper 服务器在此目录下创建一个 SEQUENCE| EPHEMERAL 类型的节点，例如“ /election/n_ ”。在 SEQUENCE 标志下， ZooKeeper 将自动地为每一个 ZooKeeper 服务器分配一个比前一个分配的序号要大的序号。此时创建节点的 ZooKeeper 服务器中拥有最小序号编号的服务器将成为 Leader 。

在实际的操作中，还需要保障：当 Leader 服务器发生故障的时候，系统能够快速地选出下一个 ZooKeeper 服务器作为 Leader 。一个简单的解决方案是，让所有的 follower 监视 leader 所对应的节点。当 Leader 发生故障时， Leader 所对应的临时节点将会自动地被删除，此操作将会触发所有监视 Leader 的服务器的 watch 。这样这些服务器将会收到 Leader 故障的消息，并进而进行下一次的 Leader 选举操作。但是，这种操作将会导致“从众效应”的发生，尤其当集群中服务器众多并且带宽延迟比较大的时候，此种情况更为明显。

在 Zookeeper 中，为了避免从众效应的发生，它是这样来实现的：每一个 follower 对 follower 集群中对应的比自己节点序号小一号的节点（也就是所有序号比自己小的节点中的序号最大的节点）设置一个 watch 。只有当 follower 所设置的 watch 被触发的时候，它才进行 Leader 选举操作，一般情况下它将成为集群中的下一个 Leader 。很明显，此 Leader 选举操作的速度是很快的。因为，每一次 Leader 选举几乎只涉及单个 follower 的操作