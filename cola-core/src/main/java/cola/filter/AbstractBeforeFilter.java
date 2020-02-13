package cola.filter;

import cola.common.RPCRequest;

/**
 * @author lunaticf
 */
public abstract class AbstractBeforeFilter {

    protected AbstractBeforeFilter next;

    public abstract void invoke(RPCRequest rpcRequest, String endPoint);
}
