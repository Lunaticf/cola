package cola.common.context;

import cola.common.RpcFuture;

import java.util.concurrent.Future;

/**
 * @author lcf
 */
public class RpcContext {

    private static final ThreadLocal<RpcContext> RPC_CONTEXT = ThreadLocal.withInitial(RpcContext::new);

    private RpcContext() {}

    private RpcFuture future;

    public static RpcContext getContext() {
        return RPC_CONTEXT.get();
    }

    public RpcFuture getFuture() {
        return future;
    }

    public void setFuture(RpcFuture future) {
        this.future = future;
    }
}


