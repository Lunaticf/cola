package cola.common.context;

import java.util.concurrent.Future;

/**
 * @author lcf
 */
public class RpcContext {

    private static final ThreadLocal<RpcContext> RPC_CONTEXT = ThreadLocal.withInitial(RpcContext::new);

    private RpcContext() {}

    private Future future;

    public static RpcContext getContext() {
        return RPC_CONTEXT.get();
    }

    public Future getFuture() {
        return future;
    }

    public void setFuture(Future future) {
        this.future = future;
    }
}


