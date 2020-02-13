package cola.cluster;

import java.util.List;

/**
 * @author lcf
 * 负载均衡
 */
public abstract class LoadBalancer {
    abstract public String select(List<String> endpoints);
}
