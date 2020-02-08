package cola.transport.netty.client;

import cola.common.AsyncProxy;
import cola.common.RpcFuture;
import cola.common.RpcRequest;
import cola.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lcf
 */
@Slf4j
public class RpcClient {
    /**
     * 客户端线程池
     */
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536), new ThreadFactory() {
        private AtomicInteger atomicInteger = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Client Thread pool - " + atomicInteger.getAndIncrement());
        }
    });

    private ServiceRegistry registry;

    public RpcClient(ServiceRegistry registry) {
        this.registry = registry;
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    /**
     * 同步调用 创建代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface}, (proxy, method, args) -> {

                    // 创建并初始化Rpc请求
                    RpcRequest request = new RpcRequest();
                    request.setRequestId(UUID.randomUUID().toString());
                    request.setClassName(method.getDeclaringClass().getName());
                    request.setMethodName(method.getName());
                    request.setParameterTypes(method.getParameterTypes());
                    request.setParameters(args);

                    String serviceName = serviceInterface.getName();

                    // 获取连接
                    RpcClientHandler handler = ConnectManager.getInstance().chooseHandler(serviceName);
                    if (handler == null) {
                        throw new RuntimeException("无服务存在");
                    }
                    RpcFuture rpcFuture = handler.sendRequest(request);
                    // 同步调用 马上就调用get堵塞
                    return rpcFuture.get();
                });
    }

    /**
     * 异步调用 返回RpcFuture
     */
    public static <T> AsyncProxy createAsyn(Class<T> serviceInterface) {
        return new AsyncProxy<>(serviceInterface);
    }
}
