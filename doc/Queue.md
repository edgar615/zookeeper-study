FIFO

1.	 Let /_QUEUE_ represent the top-level znode for our queue implementation,
which is also called the queue-node.
2.	 Clients acting as producer processes put something into the queue by
calling the create() method with the znode name as "queue-" and set the
sequence and ephemeral flags if the create() method call is set true :
create( "queue-", SEQUENCE_EPHEMERAL)
The sequence flag lets the new znode get a name like queue- N, where N is a
monotonically increasing number.
3.	 Clients acting as consumer processes process a getChildren() method call
on the queue-node with a watch event set to true :
M = getChildren(/_QUEUE_, true)
It sorts the children list M , takes out the lowest numbered child znode from
the list, starts processing on it by taking out the data from the znode, and
then deletes it.
4.	 The client picks up items from the list and continues processing on them.
On reaching the end of the list, the client should check again whether any
new items are added to the queue by issuing another get_children()
method call.
5.	 The algorithm continues when get_children() returns an empty list;
this means that no more znodes or items are left under /_QUEUE_ .

It's quite possible that in step 3, the deletion of a znode by a client will fail because
some other client has gained access to the znode while this client was retrieving the
item. In such scenarios, the client should retry the delete call.
Using this algorithm for implementation of a generic queue, we can also build
a priority queue out of it, where each item can have a priority tagged to it. The
algorithm and implementation is left as an exercise to the readers.

1、 同步队列，当一个队列的成员都聚齐时，这个队列才可用，否则一直等待所有成员到达。

2、队列按照 FIFO 方式进行入队和出队操作。

第一类，在约定目录下创建临时目录节点，监听节点数目是否是我们要求的数目。

第二类，和分布式锁服务中的控制时序场景基本原理一致，入列有编号，出列按编号。