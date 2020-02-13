package cola.cluster;

import cola.common.RpcRequest;

import java.util.List;

/**
 * @author lcf
 * 负载均衡
 */
public interface LoadBalancer {
    String select(List<String> endPoints, RpcRequest request);
}
