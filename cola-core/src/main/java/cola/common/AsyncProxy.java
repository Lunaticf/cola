package cola.common;

import cola.transport.netty.client.ConnectManager;
import cola.transport.netty.client.RpcClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author lcf
 */
@Slf4j
public class AsyncProxy<T> {
    private Class<T> service;

    public AsyncProxy(Class<T> service) {
        this.service = service;
    }

    /**
     * 对方法异步调用代理 暂时只支持包装类型
     * todo : cglib生成异步请求代理类
     */
    public RpcFuture call(String methodName, Object... args) {
        RpcClientHandler handler = ConnectManager.getInstance().chooseHandler(service.getName());
        if (handler == null) {
            throw new RuntimeException("无服务存在");
        }
        RpcRequest request = createRequest(this.service.getName(), methodName, args);
        RpcFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture;
    }

    private RpcRequest createRequest(String name, String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(name);
        request.setMethodName(methodName);
        request.setParameters(args);

        Class[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args.getClass();
        }
        request.setParameterTypes(parameterTypes);
        return request;
    }
}
