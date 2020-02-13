package cola.registry.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;


/**
 * @author lcf
 * Zk工具类
 */
class ZkSupport {

    static final int ZK_SESSION_TIMEOUT = 5000;


    /**
     * 创建一个临时znode
     * 如果路径不存在 层层创建
     */
    static void createNode(ZooKeeper zooKeeper, String path) throws KeeperException, InterruptedException {
        String[] paths = path.split("/");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < paths.length; i++) {
            if (!"".equals(paths[i])) {
                sb.append("/").append(paths[i]);
                if (zooKeeper.exists(sb.toString(), false) == null) {
                    if (i != paths.length - 1) {
                        // 如果是前面路径 我们创建永久的
                        zooKeeper.create(sb.toString(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    } else {
                        // 节点address创建临时的
                        zooKeeper.create(sb.toString(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                    }
                }
            }
        }
    }
}
