package cola.cluster.loadbalancer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lcf
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
