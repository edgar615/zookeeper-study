A group membership protocol in a distributed system enables processes to reach
a consensus on a group of processes that are currently alive and operational in
the system. Membership protocols belong to the core components of a distributed
system; they aid in maintaining service availability and consistency for the
applications. It allows other processes to know when a process joins the system and
leaves the system, thereby allowing the whole cluster to be aware of the current
system state.

A group membership protocol can be
developed using the concept of ephemeral znodes. Any client that joins the cluster
creates an ephemeral znode under a predefined path to locate memberships in the
ZooKeeper tree and set a watch on the parent path. When another node joins or
leaves the cluster, this node gets a notification and becomes aware of the change in
the group membership.
The pseudocode for the algorithm to implement this group membership protocol is
shown here.
Let a persistent znode, /membership , represent the root of the group in the
ZooKeeper tree. A group membership protocol can then be implemented as follows:
1.	 Clients joining the group create ephemeral nodes under the group root to
indicate membership.
2.	 All the members of the group will register for watch events on /membership ,
thereby being aware of other members in the group. This is done as shown in
the following code:
L = getChildren("/membership", true)
3.	 When a new client arrives and joins the group, all other members
are notified.
4.	 Similarly, when a client leaves due to failure or otherwise, ZooKeeper
automatically deletes the ephemeral znodes created in step 2. This triggers
an event, and other group members get notified.
5.	 Live members know which node joined or left by looking at the list
of children L .