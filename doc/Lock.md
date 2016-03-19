Typical scenarios where locks are inevitable are when the system as a whole needs
to ensure that only one node of the cluster is allowed to carry out an operation at a
given time, such as:
•	 Write to a shared database or file
•	 Act as a decision subsystem
•	 Process all I/O requests from other nodes

To build a distributed lock with ZooKeeper, a persistent znode is designated to be
the main lock-znode. Client processes that want to acquire the lock will create an
ephemeral znode with a sequential flag set under the lock-znode. The crux of the
algorithm is that the lock is owned by the client process whose child znode has the
lowest sequence number. ZooKeeper guarantees the order of the sequence number,
as sequence znodes are numbered in a monotonically increasing order. Suppose
there are three znodes under the lock-znode: l1, l2, and l3. The client process that
created l1 will be the owner of the lock. If the client wants to release the lock, it
simply deletes l1, and then the owner of l2 will be the lock owner, and so on.

Let the parent lock node be represented by a persistent znode, /_locknode_ , in the
Zookeeper tree.
Phase 1: Acquire a lock with the following steps:
1.	 Call the create("/_locknode_/lock-",CreateMode=EPHEMERAL_
SEQUENTIAL) method.
2.	 Call the getChildren("/_locknode_/lock-", false) method on the
lock node. Here, the watch flag is set to false , as otherwise it can lead to
a herd effect.
3.	 If the znode created by the client in step 1 has the lowest sequence number
suffix, then the client is owner of the lock, and it exits the algorithm.
4.	 Call the exists("/_locknode_/<znode path with next lowest
sequence number>, True) method.
5.	 If the exists() method returns false , go to step 2.
6.	 If the exists() method returns true , wait for notifications for the watch
event set in step 4.
Phase 2: Release a lock as follows:
1.	 The client holding the lock deletes the node, thereby triggering the next client
in line to acquire the lock.
2.	 The client that created the next higher sequence node will be notified and
hold the lock. The watch for this event was set in step 4 of Phase 1: Acquire
a lock.

While it's not recommended that you use a distributed system with a large number
of clients due to the herd effect, if the other clients also need to know about the
change of lock ownership, they could set a watch on the /_locknode_ lock node for
events of the NodeChildrenChanged type and can determine the current owner.
If there was a partial failure in the creation of znode due to connection loss, it's
possible that the client won't be able to correctly determine whether it successfully
created the child znode. To resolve such a situation, the client can store its session ID
in the znode data field or even as a part of the znode name itself. As a client retains
the same session ID after a reconnect, it can easily determine whether the child znode
was created by it by looking at the session ID.