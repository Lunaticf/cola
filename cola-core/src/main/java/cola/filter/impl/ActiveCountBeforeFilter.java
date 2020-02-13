package cola.filter.impl;

import cola.common.RpcRequest;
import cola.common.context.RpcStatic;
import cola.filter.AbstractBeforeFilter;


/**
 * @author lcf
 */
public class ActiveCountBeforeFilter extends AbstractBeforeFilter {


    @Override
    public void invoke(RpcRequest rpcRequest, String endPoint) {

        RpcStatic.incCount(rpcRequest.getInterfaceName(), rpcRequest.getMethodName(), endPoint);

        // 调用下一个
        if (next != null) {
            next.invoke(rpcRequest, endPoint);
        }
    }
}
