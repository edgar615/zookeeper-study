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