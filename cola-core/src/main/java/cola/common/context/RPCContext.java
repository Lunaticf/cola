package cola.common.context;

import cola.common.RPCFuture;

import java.util.concurrent.Future;

/**
 * @author lcf
 */
public class RPCContext {

    private static final ThreadLocal<RPCContext> RPC_CONTEXT = ThreadLocal.withInitial(RPCContext::new);

    private RPCContext() {}

    private RPCFuture future;

    public static RPCContext getContext() {
        return RPC_CONTEXT.get();
    }

    public RPCFuture getFuture() {
        return future;
    }

    public void setFuture(RPCFuture future) {
        this.future = future;
    }
}


