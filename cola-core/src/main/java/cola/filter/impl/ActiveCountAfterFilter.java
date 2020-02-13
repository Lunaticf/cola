package cola.filter.impl;

import cola.common.RpcFuture;
import cola.common.RpcRequest;
import cola.common.RpcResponse;
import cola.common.context.RpcStatic;
import cola.filter.AbstractAfterFilter;
import cola.transport.netty.client.RpcFutureManager;

/**
 * @author lcf
 */
public class ActiveCountAfterFilter extends AbstractAfterFilter {


    @Override
    public void invoke(RpcResponse rpcResponse, String endPoint) {
        RpcRequest request = RpcFutureManager.getInstance()
                .getFuture(rpcResponse.getRequestId())
                .getRequest();
        RpcStatic.decCount(request.getInterfaceName(), request.getMethodName(), endPoint);
        
        // 调用下一个
        if (next != null) {
            next.invoke(rpcResponse, endPoint);
        }

    }
}
