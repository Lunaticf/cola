package cola.registry.zookeeper;

import cola.registry.ServiceRegistry;
import cola.transport.netty.client.ConnectManager;
import cola.transport.netty.client.RpcClient;
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
import java.util.concurrent.TimeUnit;

/**
 * @author lcf
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    private ZooKeeper zooKeeper;

    /**
     * 本地缓存
     */
    private ConcurrentHashMap<String, List<String>> addressCache = new ConcurrentHashMap<>();

    public ZkServiceRegistry(String registryAddress) {
        connectServer(registryAddress);
    }

    /**
     * 连接ZooKeeper
     */
    private void connectServer(String registryAddress) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            // 这里是异步的 所以要用latch
            zooKeeper = new ZooKeeper(registryAddress, ZkSupport.ZK_SESSION_TIMEOUT, event -> {
                // 如果成功连接
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            });

            // 10s连接不上报错
            boolean status = latch.await(1, TimeUnit.SECONDS);
            if (!status) {
                log.error("连接ZooKeeper失败");
            }
        } catch (IOException | InterruptedException e) {
            log.error("连接ZooKeeper失败, {}", e);
        }
    }

    /**
     * 获取服务
     */
    @Override
    public List<String> discover(String service) {
        // 已经监听 直接从本地缓存获取
        if (addressCache.containsKey(service)) {
            log.info("从本地缓存获取服务地址");
            return addressCache.get(service);
        }

        // 从ZooKeeper获取 并且设置监听
        watchNode(service, genServicePath(service));
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
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    @Override
    public void registry(String service, String address) {
        String path = generatePath(service, address);
        try {
            ZkSupport.createNode(zooKeeper, path);
        } catch (KeeperException | InterruptedException e) {
            log.error("创建znode失败", path);
        }
    }
}
