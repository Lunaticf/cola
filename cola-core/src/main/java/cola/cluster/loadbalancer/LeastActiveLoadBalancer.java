package cola.cluster.loadbalancer;

import cola.cluster.LoadBalancer;
import cola.common.RPCRequest;
import cola.common.context.RPCStatic;

import java.util.List;

/**
 * @author lcf
 * @date 2020-02-13 22:00
 */
public class LeastActiveLoadBalancer implements LoadBalancer {
    @Override
    public String select(List<String> endPoints, RPCRequest request) {
        String endpoint = null;
        int least = 0;
        for (String candidate : endPoints) {
            int current = RPCStatic.getCount(request.getInterfaceName(), request.getMethodName(), candidate);
            if (endpoint == null || current < least) {
                endpoint = candidate;
                least = current;
            }
        }
        return endpoint;
    }
}
