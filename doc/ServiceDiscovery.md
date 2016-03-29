Important properties of a service discovery system are mentioned here:
•	 It allows services to register their availability
•	 It provides a mechanism to locate a live instance of a particular service
•	 It propagates a service change notification when the instances of a
service change

Let /services represent the base path in the ZooKeeper tree for services of the
system or platform. Persistent znodes under /services designate services available
to be consumed by clients.
A simple service discovery model with ZooKeeper is illustrated as follows:
•	 Service registration: For service registrations, hosts that serve a particular
service create an ephemeral znode in the relevant path under /services .
For example, if a server is hosting a web-caching service, it creates an
ephemeral znode with its hostname in /services/web_cache . Again, if
some other server hosts a file-serving service, it creates another ephemeral
znode with its hostname in /services/file_server and so on.
•	 Service discovery: Now, clients joining the system, register for watches in the
znode path for the particular service. If a client wants to know the servers
in the infrastructure that serve a web-caching service, the client will keep a
watch in /services/web_cache .
If a new host is added to serve web caching under this path, the client will
automatically know the details about the new location. Again, if an existing
host goes down, the client gets the event notification and can take the
necessary action of connecting to another host.

1.	在zookeeper中创建一个/services节点表示服务注册信息的根节点，所有在/services节点下的节点都代表一个可用的服务提供方。
2.	服务注册：所有的服务提供方都在/services节点下创建一个临时节点/<服务名>/<节点ID>，例如我们有两个服务：缓存服务和文件服务，那么我们需要在/services节点下创建一个/cache的节点代表缓存服务，创建一个/file的节点代表文件服务。当有一个缓存服务启动之后，会在/cache节点下创建一个临时节点，并将服务提供方的信息写到这个临时节点上。当这个缓存服务不可用时，会断开与Zookeeper之间的连接，Zookeeper会检测到连接断开后，会自动将这个临时节点删除
3.	服务发现：服务消费者需要使用缓存服务，它会观察/services/cache节点的变化，如果一个缓存服务加入/services/cache节点，Zookeeper会将这个变化推送给服务消费者，服务消费者可以读取这个临时节点保存的服务提供方信息，并保存在自己的缓存中。同理，当有缓存服务从/services/cache节点中删除后，服务消费者也可以读取到这个变化，并删除本地保存的服务提供方信息
