package cola.transport.netty.client;

import cola.common.RPCFuture;
import cola.common.RPCResponse;
import cola.filter.FilterManager;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lcf
 */
public class RPCFutureManager {

    /**
     * Singleton
     */
    private static class SingletonHolder {
        private static RPCFutureManager rpcFutureManager = new RPCFutureManager();
    }

    public static RPCFutureManager getInstance() {
        return SingletonHolder.rpcFutureManager;
    }

    private ConcurrentHashMap<String, RPCFuture> rpcFutureMap = new ConcurrentHashMap<>();

    public void registerFuture(RPCFuture rpcFuture) {
        rpcFutureMap.put(rpcFuture.getRequest().getRequestId(), rpcFuture);
    }

    public void futureDone(RPCResponse response) {
        RPCFuture rpcFuture = rpcFutureMap.remove(response.getRequestId());
        rpcFuture.done(response);
    }

    public RPCFuture getFuture(String requestId) {
        return rpcFutureMap.get(requestId);
    }
}
