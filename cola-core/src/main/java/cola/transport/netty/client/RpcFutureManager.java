package cola.transport.netty.client;

import cola.common.RpcFuture;
import cola.common.RpcResponse;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lcf
 */
public class RpcFutureManager {

    /**
     * Singleton
     */
    private static class SingletonHolder {
        private static RpcFutureManager rpcFutureManager = new RpcFutureManager();
    }

    public static RpcFutureManager getInstance() {
        return SingletonHolder.rpcFutureManager;
    }

    private ConcurrentHashMap<String, RpcFuture> rpcFutureMap = new ConcurrentHashMap<>();

    public void registerFuture(RpcFuture rpcFuture) {
        rpcFutureMap.put(rpcFuture.getRequest().getRequestId(), rpcFuture);
    }

    public void futureDone(RpcResponse response) {
        rpcFutureMap.remove(response.getRequestId()).done(response);
    }
}
