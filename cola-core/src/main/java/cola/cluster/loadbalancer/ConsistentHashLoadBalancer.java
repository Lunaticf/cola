package cola.cluster.loadbalancer;

import cola.cluster.LoadBalancer;
import cola.common.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author lcf
 */
@Slf4j
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一个真实节点对应5个虚拟节点
     */
    private static final int VIRTUAL_NODES = 5;

    private List<String> cachedEndpoints;

    /**
     * 哈希环
     */
    private final SortedMap<Integer, String> circle = new TreeMap<>();

    @Override
    public String select(List<String> endPoints, RpcRequest request) {
        // 如果跟缓存的列表不一样了 就重新计算哈希环
        if (cachedEndpoints == null || !cachedEndpoints.containsAll(endPoints)) {
            buildCircle(endPoints);
            cachedEndpoints = endPoints;
        }

        return getServer(request.key());
    }

    private void buildCircle(List<String> endPoints) {
        for (int i = 0; i < endPoints.size(); i++) {
            for (int j = 0; j < VIRTUAL_NODES; j++) {
                String virtualNodeName = endPoints.get(i) + "&&VN" + i;
                int hash = getHash(virtualNodeName);
                log.debug("虚拟节点[" + virtualNodeName + "]被添加, hash值为" + hash);
                circle.put(hash, virtualNodeName);
            }
        }
    }

    /**
     * 根据请求的方法和参数 路由到相应的环上节点
     */
    private String getServer(String key) {
        int hash = getHash(key);

        // 得到大于该hash值的所有键
        SortedMap<Integer, String> subMap = circle.tailMap(hash);

        String virtualNode;
        if (subMap.isEmpty()) {
            virtualNode = circle.get(circle.firstKey());
        } else {
            virtualNode = subMap.get(subMap.firstKey());
        }

        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }


    /**
     * FNV1_32_HASH 算法计算服务器的Hash值
     * https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function
     */
    private static int getHash(String str){
        final int p = 16777619;
        int hash = (int)2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

}
