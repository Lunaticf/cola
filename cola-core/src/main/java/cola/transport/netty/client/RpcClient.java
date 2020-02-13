package cola.transport.netty.client;

import cola.cluster.LoadBalancer;
import cola.common.RpcFuture;
import cola.common.RpcRequest;
import cola.common.RpcResponse;
import cola.common.context.RpcContext;
import cola.common.enumeration.InvokeType;
import cola.registry.ServiceRegistry;
import cola.serialization.Serializer;
import cola.util.StringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lcf
 */
@Slf4j
public class RpcClient {

    private ServiceRegistry serviceRegistry;

    private LoadBalancer loadBalancer;

    private ConnectManager connectManager;

    /**
     * 客户端线程池
     */
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536), new ThreadFactory() {
        private AtomicInteger atomicInteger = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Client Thread pool - " + atomicInteger.getAndIncrement());
        }
    });

    public RpcClient(Serializer serializer, ServiceRegistry serviceRegistry, LoadBalancer loadBalancer) {
        this.serviceRegistry = serviceRegistry;
        this.loadBalancer = loadBalancer;
        this.connectManager = new ConnectManager(serializer);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    /**
     * 创建代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceInterface, InvokeType invokeType, int timeout) {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface}, (proxy, method, args) -> {

                    // 创建并初始化Rpc请求
                    RpcRequest request = RpcRequest.builder()
                            .requestId(UUID.randomUUID().toString())
                            .interfaceName(method.getDeclaringClass().getName())
                            .methodName(method.getName())
                            .parameterTypes(method.getParameterTypes())
                            .parameters(args).build();

                    String serviceName = serviceInterface.getName();

                    // 获取服务地址
                    List<String> endPoints = serviceRegistry.discover(serviceName);

                    // 负载均衡获取一个地址
                    String seleted = loadBalancer.select(endPoints);

                    // 获取连接
                    Channel channel = connectManager.getChannel(StringUtil.str2socket(seleted));

                    // oneway sync async
                    if (invokeType == InvokeType.ONEWAY) {
                        RpcContext.getContext().setFuture(null);
                        sendRequest(channel, request);
                        return null;
                    } else if (invokeType == InvokeType.SYNC) {
                        RpcFuture rpcFuture = new RpcFuture(request, timeout);
                        RpcContext.getContext().setFuture(null);
                        RpcFutureManager.getInstance().registerFuture(rpcFuture);
                        // 同步调用 马上就调用get堵塞
                        return rpcFuture.get();
                    } else {
                        RpcFuture rpcFuture = new RpcFuture(request, timeout);
                        RpcContext.getContext().setFuture(rpcFuture);
                        RpcFutureManager.getInstance().registerFuture(rpcFuture);
                        // 异步调用
                        return null;
                    }
                });
    }



    private void sendRequest(Channel channel, RpcRequest request) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        channel.writeAndFlush(request).addListener(
                (ChannelFutureListener) future -> latch.countDown()
        );

        latch.await();
        log.debug("发送成功 {}", request);
    }





//    /**
//     * 异步调用 返回RpcFuture
//     */
//    public <T> AsyncProxy createAsyn(Class<T> serviceInterface) {
//        return new AsyncProxy<>(serviceInterface);
//    }
//
//
//    public class AsyncProxy<T> {
//        private Class<T> service;
//
//        public AsyncProxy(Class<T> service) {
//            this.service = service;
//        }
//
//        public RpcFuture call(String methodName, Object... args) throws Exception {
//            // 获取服务地址
//            List<String> endPoints = serviceRegistry.discover(service.getName());
//
//            // 负载均衡获取一个地址
//            String seleted = loadBalancer.select(endPoints);
//
//            // 获取连接
//            Channel channel = connectManager.getChannel(StringUtil.str2socket(seleted));
//
//            RpcRequest request = createRequest(service.getName(), methodName, args);
//
//            RpcFuture rpcFuture = new RpcFuture(request);
//            RpcFutureManager.getInstance().registerFuture(rpcFuture);
//
//            CountDownLatch latch = new CountDownLatch(1);
//            channel.writeAndFlush(request).addListener(
//                    (ChannelFutureListener) future -> latch.countDown()
//            );
//            return rpcFuture;
//        }
//
//        private RpcRequest createRequest(String name, String methodName, Object[] args) {
//            Class[] parameterTypes = new Class[args.length];
//            for (int i = 0; i < args.length; i++) {
//
//                parameterTypes[i] = args[i].getClass();
//            }
//
//            RpcRequest request = RpcRequest.builder()
//                    .requestId(UUID.randomUUID().toString())
//                    .interfaceName(name)
//                    .methodName(name)
//                    .parameters(args)
//                    .parameterTypes(parameterTypes).build();
//            return request;
//        }
//    }
}
