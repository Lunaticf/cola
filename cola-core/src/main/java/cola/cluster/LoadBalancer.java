package cola.cluster;

import cola.common.RPCRequest;

import java.util.List;

/**
 * @author lcf
 * 负载均衡
 */
public interface LoadBalancer {
    String select(List<String> endPoints, RPCRequest request);
}
