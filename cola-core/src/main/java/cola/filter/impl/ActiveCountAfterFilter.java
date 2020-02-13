package cola.filter.impl;

import cola.common.RPCFuture;
import cola.common.RPCRequest;
import cola.common.RPCResponse;
import cola.common.context.RPCStatic;
import cola.filter.AbstractAfterFilter;
import cola.transport.netty.client.RPCFutureManager;

/**
 * @author lcf
 */
public class ActiveCountAfterFilter extends AbstractAfterFilter {


    @Override
    public void invoke(RPCResponse rpcResponse, String endPoint) {
        RPCRequest request = RPCFutureManager.getInstance()
                .getFuture(rpcResponse.getRequestId())
                .getRequest();
        RPCStatic.decCount(request.getInterfaceName(), request.getMethodName(), endPoint);
        
        // 调用下一个
        if (next != null) {
            next.invoke(rpcResponse, endPoint);
        }

    }
}
