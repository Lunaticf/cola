package cola.registry.zookeeper;

import cola.registry.ServiceRegistry;
import cola.transport.netty.client.ConnectManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author lcf
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    private ZooKeeper zooKeeper;

    /**
     * ZooKeeper的ip
     */
    private String registryAddress;

    /**
     * 本地缓存
     */
    private ConcurrentHashMap<String, List<String>> addressCache = new ConcurrentHashMap<>();

    /**
     * 记录是否已经监听某个service
     */
    private ConcurrentHashMap<String, Boolean> listener = new ConcurrentHashMap<>();


    public ZkServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    @PostConstruct
    @Override
    public void init() {
        // 初始化ZooKeeper连接
        if (registryAddress == null) {
            throw new RuntimeException("ZooKeeper初始化连接地址为空");
        }
        connectServer();
        if (zooKeeper != null) {
            log.info("connect to ZooKeeper");
        }
    }

    private void connectServer() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            // 这里是异步的 所以要用latch
            zooKeeper = new ZooKeeper(registryAddress, ZkSupport.ZK_SESSION_TIMEOUT, event -> {
                // 如果成功连接
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            log.error("连接ZooKeeper失败");
        }
    }

    @Override
    public List<String> discover(String service) {
        // 已经监听 直接从本地缓存获取
        if (listener.containsKey(service)) {
            log.info("从本地缓存获取服务地址");
            return addressCache.get(service);
        }

        // 从ZooKeeper获取 并且设置监听
        watchNode(service, ZkSupport.genServicePath(service));
        log.info("监听服务", service);
        listener.put(service, true);
        return addressCache.get(service);
    }

    /**
     * 监听一个节点
     */
    private void watchNode(String service, String path) {
        try {
            List<String> addresses = zooKeeper.getChildren(path, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    watchNode(service, path);
                }
            });
            addressCache.put(service, addresses);
            updateConnectedServer(service, addresses);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    private void updateConnectedServer(String service, List<String> addresses) {
        ConnectManager.getInstance().updateConnectedServer(service, addresses);
    }

    @Override
    public void registry(String service, String address) {
        String path = ZkSupport.generatePath(service, address);
        try {
            ZkSupport.createNode(zooKeeper, path);
        } catch (KeeperException | InterruptedException e) {
            log.error("创建znode失败", path);
        }
    }

    @Override
    public void stop() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
