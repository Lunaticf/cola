package cola.cluster.loadbalancer;

import cola.cluster.LoadBalancer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lcf
 * 随机负载均衡
 */
public class RandomLoadBalancer extends LoadBalancer {
    @Override
    public String select(List<String> endpoints) {
        if (endpoints.isEmpty()) {
            return null;
        }
        return endpoints.get(ThreadLocalRandom.current().nextInt(endpoints.size()));
    }
}
