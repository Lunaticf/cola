package cola.filter;

import cola.common.RPCResponse;

/**
 * @author lcf
 */
public abstract class AbstractAfterFilter {

    protected AbstractAfterFilter next;

    public abstract void invoke(RPCResponse rpcResponse, String endPoint);
}
