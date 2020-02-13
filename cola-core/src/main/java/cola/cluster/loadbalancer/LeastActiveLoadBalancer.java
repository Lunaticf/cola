package cola.cluster.loadbalancer;

import cola.cluster.LoadBalancer;
import cola.common.RpcRequest;
import cola.common.context.RpcStatic;

import java.util.List;

/**
 * @author lcf
 * @date 2020-02-13 22:00
 */
public class LeastActiveLoadBalancer implements LoadBalancer {
    @Override
    public String select(List<String> endPoints, RpcRequest request) {
        String endpoint = null;
        int least = 0;
        for (String candidate : endPoints) {
            int current = RpcStatic.getCount(request.getInterfaceName(), request.getMethodName(), candidate);
            if (endpoint == null || current < least) {
                endpoint = candidate;
                least = current;
            }
        }
        return endpoint;
    }
}
