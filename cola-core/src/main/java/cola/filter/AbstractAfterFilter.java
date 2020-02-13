package cola.filter;

import cola.common.RpcResponse;

/**
 * @author lcf
 */
public abstract class AbstractAfterFilter {

    protected AbstractAfterFilter next;

    public abstract void invoke(RpcResponse rpcResponse, String endPoint);
}
