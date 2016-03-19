package com.edgar.zookeeper.config;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;

public class ConfigWatcher implements Watcher {

  public ConfigWatcher(String hosts) throws IOException, InterruptedException {
    this.store = new ActiveKeyValueStore();
    store.connect(hosts);
  }

    private ActiveKeyValueStore store;

    public void displayConfig() throws KeeperException, InterruptedException {
        String value = store.read(ConfigUpdater.PATH, this);
        System.out.printf("Read %s as %s\n", ConfigUpdater.PATH, value);
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDataChanged) {
            try {
                displayConfig();
            } catch (KeeperException e) {
                System.out.printf("KeeperException: %s", e);
            } catch (InterruptedException e) {
                System.out.printf("InterruptedException");
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ConfigWatcher configWatcher = new ConfigWatcher("localhost:2181");
        configWatcher.displayConfig();

        Thread.sleep(Long.MAX_VALUE);
    }
}