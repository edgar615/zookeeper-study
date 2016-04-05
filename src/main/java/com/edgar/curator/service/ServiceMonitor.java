package com.edgar.curator.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.UriSpec;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/4/5.
 *
 * @author Edgar  Date 2016/4/5
 */
public class ServiceMonitor {
  public static void main(String[] args) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory
            .newClient("10.4.7.48:2181", new RetryOneTime(1000));
    client.start();

    ServiceDiscovery<Void> serviceDiscovery =  ServiceDiscoveryBuilder.builder(Void.class).basePath
            ("/service-demo")
            .client(client)
            .build();

    ServiceProvider<Void> serviceProvider =
            serviceDiscovery.serviceProviderBuilder()//.providerStrategy()
                    .serviceName("demo")
    .build();

    serviceProvider.start();

    for (int i = 0; i < 10; i ++) {
      ServiceInstance instance = serviceProvider.getInstance();
      String address = instance.buildUriSpec();
      //String response = //address + "/api";
      System.out.println(address + "/api");
    }

    TimeUnit.SECONDS.sleep(10);
  }
}
