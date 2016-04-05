package com.edgar.curator.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class ServiceRegister {
  public static void main(String[] args) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory
            .newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();

    ServiceInstance<Void> serviceInstance = ServiceInstance.<Void>builder().name("demo")
            .port(8080).address("10.4.7.15")
            .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
            .build();

    ServiceDiscoveryBuilder.builder(Void.class).basePath("/service-demo")
            .client(client)
            .thisInstance(serviceInstance)
            .build()
            .start();

    TimeUnit.SECONDS.sleep(10);
  }
}
