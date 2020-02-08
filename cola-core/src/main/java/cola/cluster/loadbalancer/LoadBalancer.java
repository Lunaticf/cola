package cola.cluster.loadbalancer;

import java.util.List;

/**
 * @author lcf
 */
public abstract class LoadBalancer {
    abstract public String select(List<String> endpoints);
}
