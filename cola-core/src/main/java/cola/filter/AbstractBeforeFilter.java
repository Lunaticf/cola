package cola.filter;

import cola.common.RpcRequest;

/**
 * @author lunaticf
 */
public abstract class AbstractBeforeFilter {

    protected AbstractBeforeFilter next;

    public abstract void invoke(RpcRequest rpcRequest, String endPoint);
}
