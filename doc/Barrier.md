Barrier
Barrier is a type of synchronization method used in distributed systems to block the
processing of a set of nodes until a condition is satisfied. It defines a point where all
nodes must stop their processing and cannot proceed until all the other nodes reach
this barrier.

The algorithm to implement a barrier using ZooKeeper is as follows:
1.	 To start with, a znode is designated to be a barrier znode, say /zk_barrier .
2.	 The barrier is said to be active in the system if this barrier znode exists.
3.	 Each client calls the ZooKeeper API's exists() function on /zk_barrier
by registering for watch events on the barrier znode (the watch event is set
to true ).
4.	 If the exists() method returns false , the barrier no longer exists, and the
client proceeds with its computation.
5.	 Else, if the exists() method returns true , the clients just waits for
watch events.
6.	 Whenever the barrier exit condition is met, the client in charge of the barrier
will delete /zk_barrier .
7.	 The deletion triggers a watch event, and on getting this notification, the client
calls the exists() function on /zk_barrier again.
8.	 Step 7 returns true , and the clients can proceed further.


double barrier
There is another type of barrier that aids in synchronizing the
beginning and end of a computation; this is known as a double barrier. The logic
of a double barrier states that a computation is started when the required number
of processes join the barrier. The processes leave after completing the computation,
and when the number of processes participating in the barrier become zero, the
computation is stated to end.

Phase 1: Joining the barrier znode can be done as follows:
1.	 Suppose the barrier znode is represented by znode/barrier . Every client
process registers with the barrier znode by creating an ephemeral znode
with /barrier as the parent. In real scenarios, clients might register using
their hostnames.
2.	 The client process sets a watch event for the existence of another znode called
ready under the /barrier znode and waits for the node to appear.
3.	 A number N is predefined in the system; this governs the minimum number
of clients to join the barrier before the computation can start.
4.	 While joining the barrier, each client process finds the number of child
znodes of /barrier :
M = getChildren(/barrier, watch=false)
5.	 If M is less than N, the client waits for the watch event registered in step 3
6.	 Else, if M is equal to N, then the client process creates the ready znode under
/barrier .
7.	 The creation of the ready znode in step 5 triggers the watch event, and each
client starts the computation that they were waiting so far to do.
Phase 2: Leaving the barrier can be done as follows:
1.	 Client processing on finishing the computation deletes the znode it created
under /barrier (in step 2 of Phase 1: Joining the barrier).
2.	 The client process then finds the number of children under /barrier :
M = getChildren(/barrier, watch=True)
If M is not equal to 0, this client waits for notifications (observe that we have
set the watch event to True in the preceding call).
If M is equal to 0, then the client exits the barrier znode.