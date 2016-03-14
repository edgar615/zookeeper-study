Clients
can connect to a ZooKeeper service by connecting to any member of the ensemble.
You can send and receive requests and responses as well as event notifications
between clients and the service, which are all done by maintaining a TCP connection
and by periodically sending heartbeats.

With this, ZooKeeper maintains a strict ordering of its transactions, which enables
the implementation of advanced distributed synchronization primitives that are
simple and reliable. With its design to be robust, reliable, high performing, and
fast, this coordination service makes it possible to be used in large and complex
distributed applications.


ZNODE

The data in a znode is typically stored in a byte format, with a maximum data size
in each znode of no more than 1 MB. ZooKeeper is designed for coordination, and
almost all forms of coordination data are relatively small in size; hence, this limit on
the size of data is imposed. It is recommended that the actual data size be much less
than this limit as well.

Like files in a filesystem, znodes maintain a stat structure that includes version
numbers for data changes and an access control list that changes along with
timestamps associated with changes. The version number increases whenever the
znode's data changes. ZooKeeper uses the version numbers along with the associated
timestamps to validate its in-core cache. The znode version number also enables
the client to update or delete a particular znode through ZooKeeper APIs. If the
version number specified doesn't match the current version of a znode, the operation
fails. However, this can be overridden by specifying 0 as the version number while
performing a znode update or delete operation.


Types of znodes
ZooKeeper has two types of znodes: persistent and ephemeral. There is a third type
that you might have heard of, called a sequential znode, which is a kind of a qualifier
for the other two types. Both persistent and ephemeral znodes can be sequential
znodes as well. Note that a znode's type is set at its creation time.


As the name suggests, persistent znodes have a lifetime in the ZooKeeper's namespace
until they're explicitly deleted. A znode can be deleted by calling the delete API call.
It's not necessary that only the client that created a persistent znode has to delete it.
Note that any authorized client of the ZooKeeper service can delete a znode.

Persistent znodes are useful for storing data that needs to be highly available and
accessible by all the components of a distributed application. For example, an
application can store the configuration data in a persistent znode. The data as
well as the znode will exist even if the creator client dies.

create /[PacktPub] "ApacheZooKeeper"


By contrast, an ephemeral znode is deleted by the ZooKeeper service when the
creating client's session ends. An end to a client's session can happen because of
disconnection due to a client crash or explicit termination of the connection. Even
though ephemeral nodes are tied to a client session, they are visible to all clients,
depending on the configured Access Control List (ACL) policy.
An ephemeral znode can also be explicitly deleted by the creator client or any other
authorized client by using the delete API call. An ephemeral znode ceases to exist
once its creator client's session with the ZooKeeper service ends. Hence, in the
current version of ZooKeeper, ephemeral znodes are not allowed to have children.


create -e /[PacktPub] "ApacheZooKeeper"


The concept of ephemeral znodes can be used to build distributed applications
where the components need to know the state of the other constituent components or
resources. For example, a distributed group membership service can be implemented
by using ephemeral znodes. The property of ephemeral nodes getting deleted when
the creator client's session ends can be used as an analogue of a node that is joining or
leaving a distributed cluster. Using the membership service, any node is able discover
the members of the group at any particular time.


A sequential znode is assigned a sequence number by ZooKeeper as a part of
its name during its creation. The value of a monotonously increasing counter
(maintained by the parent znode) is appended to the name of the znode.
The counter used to store the sequence number is a signed integer (4 bytes).
It has a format of 10 digits with 0 (zero) padding. For example, look at /path/to/
znode-0000000001. This naming convention is useful to sort the sequential znodes
by the value assigned to them.


Sequential znodes can be used for the implementation of a distributed
global queue, as sequence numbers can impose a global ordering. They
may also be used to design a lock service for a distributed application.
The recipes for a distributed queue and lock service will be discussed


create -s /[PacktPub] "PersistentSequentialZnode"
create -s -e /[PacktPub] "EphemeralSequentialZnode"


## Watcher
Clients can register with the ZooKeeper service for any changes associated with
a znode. This registration is known as setting a watch on a znode in ZooKeeper
terminology. Watches allow clients to get notifications when a znode changes in
any way. **A watch is a one-time operation, which means that it triggers only one
notification. To continue receiving notifications over time, the client must reregister
the watch upon receiving each event notification.**

ZooKeeper watches are a one-time trigger. What this means is that if a client receives
a watch event and wants to get notified of future changes, it must set another watch.
Whenever a watch is triggered, a notification is dispatched to the client that had
set the watch. Watches are maintained in the ZooKeeper server to which a client is
connected, and this makes it a fast and lean method of event notification.
The watches are triggered for the following three changes to a znode:
1.	 Any changes to the data of a znode, such as when new data is written to the
znode's data field using the setData operation.
2.	 Any changes to the children of a znode. For instance, children of a znode are
deleted with the delete operation.
3.	 A znode being created or deleted, which could happen in the event that
a new znode is added to a path or an existing one is deleted.

ZooKeeper ensures that watches are always ordered in the first in first out
(FIFO) manner and that notifications are always dispatched in order
•	 Watch notifications are delivered to a client before any other change is made
to the same znode
•	 The order of the watch events are ordered with respect to the updates seen
by the ZooKeeper service


**Since ZooKeeper watches are one-time triggers and due to the
latency involved between getting a watch event and resetting of
the watch, it's possible that a client might lose changes done to a
znode during this interval.** In a distributed application in which
a znode changes multiple times between the dispatch of an event
and resetting the watch for events, developers must be careful to
handle such situations in the application logic.

When a client gets disconnected from the ZooKeeper server, it doesn't receive
any watches until the connection is re-established. If the client then reconnects,
any previously registered watches will also be reregistered and triggered. If the
client connects to a new server, the watch will be triggered for any session events.
This disconnection from a server and reconnection to a new server happens in a
transparent way for the client applications.

Although ZooKeeper guarantees that all registered watches get dispatched to the
client, even if the client disconnects from one server and reconnects to another server
within the ZooKeeper service, there is one possible scenario worth mentioning where
a watch might be missed by a client. **This specific scenario is when a client has set a
watch for the existence of a znode that has not yet been created. In this case, a watch
event will be missed if the znode is created, and deleted while the client is in the
disconnected state.**

operations

- create Creates a znode in a specified path of the ZooKeeper namespace
- delete Deletes a znode from a specified path of the ZooKeeper namespace
- exists Checks if a znode exists in the path
- getChildren Gets a list of children of a znode
- getData Gets the data associated with a znode
- setData Sets/writes data into the data field of a znode
- getACL Gets the ACL of a znode
- setACL Sets the ACL in a znode
- sync Synchronizes a client's view of a znode with ZooKeeper


ZooKeeper also supports batch updates
to znodes with an operation called **multi**. This batches together multiple primitive
operations into a single unit. A multi operation is also atomic in nature, which means
that either all the updates succeed or the whole bunch of updates fails in its entirety.

ZooKeeper does not allow partial writes or reads of the znode data. When setting
the data of a znode or reading it, the content of the znode is replaced or read
entirely. Update operations in ZooKeeper, such as a delete or setData operation,
have to specify the version number of the znode that is being updated. The version
number can be obtained by using the exists() call. The update operation will fail
if the specified version number does not match the one in the znode. Also, another
important thing to note is that updates in ZooKeeper are non-blocking operations.

-	Read requests: These are processed locally in the ZooKeeper server to which
the client is currently connected
-	Write requests: These are forwarded to the leader and go through majority
consensus before a response is generated

The write operations in ZooKeeper are atomic and durable. There is the guarantee
of a successful write operation if it has been written to persistent storage on a
majority of ZooKeeper's servers. However, the eventual consistency model of
ZooKeeper permits reads to log the latest state of the ZooKeeper service, and the
sync operation allows a client to be up-to-date with the most recent state of the
ZooKeeper service.

The following are the types of watch events that might occur during a znode
state change:
-	NodeChildrenChanged : A znode's child is created or deleted
-	NodeCreated : A znode is created in a ZooKeeper path
-	NodeDataChanged : The data associated with a znode is updated
-	NodeDeleted : A znode is deleted in a ZooKeeper path


Operation Event-generating Actions

exists A znode is created or deleted, or its data is updated

getChildren A child of a znode is created or deleted, or the znode itself is deleted

getData A znode is deleted or its data is updated

`
stat /foo0000000000
cZxid = 0x2
ctime = Mon Mar 14 21:54:44 CST 2016
mZxid = 0x2
mtime = Mon Mar 14 21:54:44 CST 2016
pZxid = 0x2
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x1000003794f0000
dataLength = 0
numChildren = 0
`

- cZxid : This is the transaction ID of the change that caused this znode to be created.
- mZxid : This is the transaction ID of the change that last modified this znode.
- pZxid : This is the transaction ID for a znode change that pertains to adding or removing children.
- ctime : This denotes the creation time of a znode in milliseconds from epoch.
- mtime : This denotes the last modification time of a znode in milliseconds from epoch.
- dataVersion : This denotes the number of changes made to the data of this znode.
- cversion : This denotes the number of changes made to the children of this znode.
- aclVersion : This denotes the number of changes made to the ACL of this znode.
- ephemeralOwner : This is the session ID of the znode's owner if the znode is an ephemeral node. If the znode is not an ephemeral node, this field is set to zero.
- dataLength : This is the length of the data field of this znode.
- numChildren : This denotes the number of children of this znode.