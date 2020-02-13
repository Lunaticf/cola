package cola.filter.impl;

import cola.common.RPCRequest;
import cola.common.context.RPCStatic;
import cola.filter.AbstractBeforeFilter;


/**
 * @author lcf
 */
public class ActiveCountBeforeFilter extends AbstractBeforeFilter {


    @Override
    public void invoke(RPCRequest rpcRequest, String endPoint) {

        RPCStatic.incCount(rpcRequest.getInterfaceName(), rpcRequest.getMethodName(), endPoint);

        // 调用下一个
        if (next != null) {
            next.invoke(rpcRequest, endPoint);
        }
    }
}
