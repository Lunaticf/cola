package cola.cluster.loadbalancer;

import cola.cluster.LoadBalancer;
import cola.common.RPCRequest;

import java.util.List;

/**
 * @author lcf
 * 简单模拟了一下RoundRobin 不带权重
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    private int index = 0;

    @Override
    public String select(List<String> endPoints, RPCRequest request) {
        if (endPoints.isEmpty()) {
            return null;
        }
        String selected = endPoints.get(index % endPoints.size());
        index = (index + 1) % endPoints.size();
        return selected;
    }
}
