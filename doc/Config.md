# Minimum configuration

•	 clientPort : This is the TCP port where clients can connect to the server.
The client port can be set to any number, and different servers can be
configured to listen on different ports. The default port is 2181.
•	 dataDir : This is the directory where ZooKeeper will store the in-memory
database snapshots. If the dataLogDir parameter is not defined separately,
the transaction logs of updates to the database would also be stored in this
directory. The myid file would also be stored in this directory if this server
is a member of an ensemble. The data directory is not very performance-
sensitive and is not required to be configured in a dedicated device if
transaction logs are stored in a different location.
•	 tickTime : This is the length of a single tick measured in milliseconds. Tick is
the basic time unit used by ZooKeeper to determine heartbeats and session
timeouts. The default tickTime parameter is 2,000 milliseconds. Lowering
the tickTime parameter enables quicker timeouts but increases network
traffic (heartbeats) and processing overhead for the ZooKeeper server.

# Storage configuration

•	 dataLogDir : This is the directory where the ZooKeeper transaction logs
are stored. The server flushes the transaction logs using sync writes. Hence,
it's very important that a dedicated transaction log device be used so that
transaction logging by the ZooKeeper server is not impacted by I/O activities
from other processes in the system. Having a dedicated log device improves
the overall throughput and assigns stable latencies to requests.
•	 preAllocSize : The zookeeper.preAllocSize Java system property is set
to preallocate the block size to the transactions log files. The default block
size is 64 MB. Preallocating the transaction log minimizes the disk seeks. If
snapshots are taken frequently, the transaction logs might not grow to 64 MB.
In such cases, we can tune this parameter to optimize the storage usage.
•	 snapCount : The zookeeper.snapCount Java system property gives us the
number of transactions between two consecutive snapshots. After snapCount
transactions are written to a logfile, a new snapshot is started, and a new
transaction logfile is created. Snapshot is a performance-sensitive operation,
and hence, having a smaller value for snapCount might negatively affect
ZooKeeper's performance. The default value of snapCount parameter is
100,000.
•	 traceFile : The requestTraceFile Java system property sets this option
to enable the logging of requests to a trace file named traceFile.year.
month.day . This option is useful for debugging, but it impacts the overall
performance of the ZooKeeper server.
•	 fsync.warningthresholdms : This is the time measured in milliseconds; it
defines a threshold for the maximum amount of time permitted to flush all
outstanding writes to the transactional log, write-ahead log (WAL). It issues
a warning message to the debug log whenever the sync operation takes
longer than this value. The default value is 1,000.
•	 autopurge.snapRetainCount : This refers to the number of snapshots
and corresponding transaction logs to retain in directories, dataDir and
dataLogDir , respectively. The default value is 3 .
•	 autopurge.purgeInterval : This refers to the time interval in hours to purge
old snapshots and transaction logs. The default value is 0 , which means auto
purging is disabled by default. We can set this option to a positive integer
(1 and above) to enable the auto purging. If it is disabled (set to 0), the
default, purging doesn't happen automatically. Manual purging can be done
by running the zkCleanup.sh script available in the bin directory of the
ZooKeeper distribution.
•	 syncEnabled : This configuration option is newly introduced in 3.4.6 and later
versions of ZooKeeper. It is set using the Java system property zookeeper.
observer.syncEnabled to enable the "observers" to log transaction and write
snapshot to disk, by default, like the "followers". Recall that observers do not
participate in the voting process unlike followers, but commit proposals from
the leader. Enabling this option reduces the recovery time of the observers on
restart. The default value is true .

# Network configuration

•	 globalOutstandingLimit : This parameter defines the maximum number of
outstanding requests in ZooKeeper. In real life, clients might submit requests
faster than ZooKeeper can process them. This happens if there are a large
number of clients. This parameter enables ZooKeeper to do flow control by
throttling clients. This is done to prevent ZooKeeper from running out of
memory due to the queued requests. ZooKeeper servers will start throttling
client requests once the globalOutstandingLimit has been reached. The
default limit is 1000 requests.
(Java system property: zookeeper.globalOutstandingLimit )
•	 maxClientCnxns : This is the maximum number of concurrent socket
connections between a single client and the ZooKeeper server. The
client is identified by its IP address. Setting up a TCP connection is a
resource-intensive operation, and this parameter is used to prevent the
overloading of the server. It is also used to prevent certain classes of DoS
attacks, including file descriptor exhaustion. The default value is 60 . Setting
this to 0 entirely removes the limit on concurrent connections.
•	 clientPortAddress : This is the IP address that listens for client connections.
By default, ZooKeeper server binds to all the interfaces for accepting client
connection.
•	 minSessionTimeout : This is the minimum session timeout in milliseconds
that the server will allow the client to negotiate. The default value is twice the
tickTime parameter. If this timeout is set to a very low value, it might result
in false positives due to incorrect detection of client failures. Setting this
timeout to a higher value will delay the detection of client failures.
•	 maxSessionTimeout : This is the maximum session timeout in milliseconds
that the server will allow the client to negotiate. By default, it is 20 times the
tickTime parameter.

# Configuring a ZooKeeper ensemble

•	 electionAlg : This option is used to choose a leader in a ZooKeeper ensemble.
A value of 0 corresponds to the original UDP-based version, 1 corresponds
to the non-authenticated UDP-based version of fast leader election, 2
corresponds to the authenticated UDP-based version of fast leader election,
and 3 corresponds to the TCP-based version of fast leader election. Currently,
algorithm 3 is the default. The implementations of leader election 0, 1, and 2
are now deprecated, and fast leader election is the only one used. Available
options are as follows
•	 initLimit : This refers to the amount of time, measured in ticks, to allow
followers to connect with the leader. initLimit should be set depending on
the network speed (and hops) between the leader and follower and based
on the amount of data to be transferred between the two. If the amount of
data stored by ZooKeeper is huge due to a large number of znodes and
the amount of data stored in them, or if the network bandwidth is low,
initLimit should be increased.
•	 syncLimit : This is the amount of time measured in ticks to allow followers to
sync with a leader. If the followers fall too far behind the leader due to server
load or network problems, they are dropped. However, the amount of data
stored by ZooKeeper has no effect on the synchronization time between the
leader and follower. Instead, syncLimit depends on network latency and
throughput.
•	 leaderServes : By default, the server in an ensemble that runs in the
leader mode also accepts client connections. However, in a loaded and
busy ensemble with an update-heavy workload, we can configure the
leader server to not accept client connections. This can be configured
using the zookeeper.leaderServes Java system property. This can aid in
coordinating write updates at a faster rate and, hence, can lead to increased
write throughput.
•	 cnxTimeout : This refers to the timeout value for opening connections
for leader election notifications. This parameter is only applicable with
leader election algorithm 3 – fast leader election . The default value
is 5 seconds.
•	 server. x =[hostname]:port1[:port2] : This parameter is used to define
servers in the ZooKeeper ensemble. When the ZooKeeper server process
starts up, it determines its identity by looking for the myid file in the data
directory. The myid file contains the server number in ASCII; this should be
the same as x in server. x of the configuration parameter. This parameter
can be further explained as follows:
° ° There are two TCP port numbers: port1 and port2 . The first port is
used to send transaction updates, and the second one is for leader
election. The leader election port is only necessary if electionAlg is
1, 2, or 3 (default). In Chapter 1, A Crash Course in Apache ZooKeeper,
we saw how to use different port numbers to test multiple servers on
a single machine.
° ° It is very important that all servers use the same server. x
configuration for proper connection to happen between them.
Also, the list of servers that make up ZooKeeper servers that is used
by the clients must match the list of ZooKeeper servers that each
ZooKeeper server has.

# Configuring a quorum

•	 group.x=nnnnn[:nnnnn] : This enables a hierarchical quorum construction.
x is a group identifier and nnnnn corresponds to server identifiers. Groups
must be disjoint, and the union of all the groups must be the ZooKeeper
ensemble.
•	 weight.x=nnnnn : This is used to assign weight to servers in a group when
forming quorums. It corresponds to the weight of a server when voting for
leader election and for the atomic broadcast protocol Zookeeper Atomic
Broadcast (ZAB). By default, the weight of a server is 1 . Assigning more
weight to a server allows it to form a quorum with other servers more easily.

# ZooKeeper best practices

•	 The ZooKeeper data directory contains the snapshot and transactional
log files. It is a good practice to periodically clean up the directory if the
autopurge option is not enabled. Also, an administrator might want to keep
a backup of these files, depending on the application needs. However, since
ZooKeeper is a replicated service, we need to back up the data of only one of
the servers in the ensemble.
•	 ZooKeeper uses Apache log4j as its logging infrastructure. As the logfiles
grow bigger in size, it is recommended that you set the auto-rollover of the
logfiles using the in-built log4j feature for ZooKeeper logs.
•	 The list of ZooKeeper servers used by the clients in their connection strings
must match the list of ZooKeeper servers that each ZooKeeper server has.
Strange behaviors might occur if the lists don't match.
•	 The server lists in each Zookeeper server configuration file should be
consistent with the other members of the ensemble.
•	 As already mentioned, the ZooKeeper transaction log must be configured in
a dedicated device. This is very important to achieve best performance from
ZooKeeper.
•	 The Java heap size should be chosen with care. Swapping should never be
allowed to happen in the ZooKeeper server. It is better if the ZooKeeper
servers have a reasonably high memory (RAM).
•	 System monitoring tools such as vmstat can be used to monitor virtual
memory statistics and decide on the optimal size of memory needed,
depending on the need of the application. In any case, swapping should
be avoided.