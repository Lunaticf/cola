package cola.transport.netty.client;

import cola.cluster.LoadBalancer;
import cola.cluster.loadbalancer.LeastActiveLoadBalancer;
import cola.common.RpcFuture;
import cola.common.RpcRequest;
import cola.common.context.RpcContext;
import cola.common.context.RpcStatic;
import cola.common.enumeration.InvokeType;
import cola.filter.AbstractAfterFilter;
import cola.filter.AbstractBeforeFilter;
import cola.filter.FilterManager;
import cola.registry.ServiceRegistry;
import cola.serialization.Serializer;
import cola.util.StringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
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
     * 默认同步调用
     */
    public <T> T create(Class<T> serviceInterface) {
        return create(serviceInterface, InvokeType.SYNC);
    }

    /**
     * 创建代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceInterface, InvokeType invokeType) {
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
                    String selected = loadBalancer.select(endPoints, request);

                    if (selected == null){
                        throw new RuntimeException("对应服务不存在实例");
                    }

                    // 获取连接
                    Channel channel = connectManager.getChannel(StringUtil.str2socket(selected));

                    // before filter
                    if (FilterManager.beforeFilter != null) {
                        FilterManager.beforeFilter.invoke(request, selected);
                    }

                    // oneway sync async
                    if (invokeType == InvokeType.ONEWAY) {
                        RpcContext.getContext().setFuture(null);
                        sendRequest(channel, request);
                        return null;
                    } else if (invokeType == InvokeType.SYNC) {
                        RpcFuture rpcFuture = new RpcFuture(request);
                        RpcContext.getContext().setFuture(rpcFuture);
                        RpcFutureManager.getInstance().registerFuture(rpcFuture);
                        sendRequest(channel, request);
                        // 同步调用 马上就调用get堵塞
                        return rpcFuture.get();
                    } else {
                        RpcFuture rpcFuture = new RpcFuture(request);
                        RpcContext.getContext().setFuture(rpcFuture);
                        RpcFutureManager.getInstance().registerFuture(rpcFuture);
                        sendRequest(channel, request);
                        // 异步调用
                        return null;
                    }
                });
    }



    private void sendRequest(Channel channel, RpcRequest request) throws InterruptedException {
        log.debug("开始发送Rpc请求");
        CountDownLatch latch = new CountDownLatch(1);

        channel.writeAndFlush(request).addListener(
                (ChannelFutureListener) future -> latch.countDown()
        );

        latch.await();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();

        // 活跃计数
        if (loadBalancer instanceof LeastActiveLoadBalancer) {
            RpcStatic.incCount(request.getInterfaceName(), request.getMethodName(), socketAddress.getAddress() + ":" + socketAddress.getPort());
        }

        log.debug("发送成功 {}", request);
    }

    public void addBeforeFilter(AbstractBeforeFilter filter) {
        FilterManager.addBeforeFilter(filter);
    }

    public void addAfterFilter(AbstractAfterFilter filter) {
        FilterManager.addAfterFilter(filter);
    }

}
