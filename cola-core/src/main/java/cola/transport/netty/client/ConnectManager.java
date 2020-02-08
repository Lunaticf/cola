package cola.transport.netty.client;

import cola.cluster.loadbalancer.LoadBalancer;
import cola.registry.ServiceRegistry;
import cola.serialization.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author lcf
 * Rpc客户端网络连接器 维护连接 发起连接
 * 还要监听失效的服务器 删除失效的连接 这一步比较麻烦
 */
@Slf4j
public class ConnectManager {
    /**
     *  序列化器
     */
    private Serializer serializer;

    private ServiceRegistry serviceRegistry;

    private LoadBalancer loadBalancer;

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

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    /**
     * 所有连接
     */
    private CopyOnWriteArrayList<RpcClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();

    /**
     * 记录服务器对应的服务 如果一个服务器服务为0了 关闭该服务器的连接
     */
    private Map<String, Set<String>> host2service = new ConcurrentHashMap<>();

    /**
     * 记录每个服务器对应的连接
     */
    private Map<InetSocketAddress, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private long connectTimeoutMillis = 6000;
    private volatile boolean isRuning = true;

    /**
     * 单例模式静态内部类实现
     */
    private static class SingletonHolder {
        private static ConnectManager instance = new ConnectManager();
    }

    public static ConnectManager getInstance() {
        return SingletonHolder.instance;
    }


    /**
     * 需要把挂掉的服务的连接全部关闭
     */
    public void updateConnectedServer(String service, List<String> addresses) {
        Set<String> addressSet = new HashSet<>(addresses);

        // 先添加service
        for (String address : addresses) {
            if (!host2service.containsKey(address)) {
                Set<String> set = new HashSet<>();
                set.add(service);
                host2service.put(address, set);

                // 对新的服务器发起连接
                InetSocketAddress remotePeer = str2socket(address);
                connectServerNode(remotePeer);
            } else {
                host2service.get(address).add(service);
            }
        }

        // 移除host失效的服务
        for (String host : host2service.keySet()) {
            Set<String> services = host2service.get(host);
            // 如果该host不在最新的列表
            if (services.contains(service) && !addressSet.contains(host)) {
                services.remove(service);
                if (services.size() == 0) {
                    // 该服务器上已经没有valid 服务
                    InetSocketAddress socketAddress = str2socket(host);

                    // 关闭该服务器对应的连接
                    RpcClientHandler handler = connectedServerNodes.get(socketAddress);
                    if (handler != null) {
                        handler.close();
                    }
                    connectedServerNodes.remove(socketAddress);
                    connectedHandlers.remove(handler);
                }
            }
        }

    }

    /**
     * 发起连接 样版代码
     */
    private void connectServerNode(InetSocketAddress remotePeer) {
        if (connectedServerNodes.containsKey(remotePeer)) {
            return;
        }
        threadPoolExecutor.submit(() -> {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientInitializer(serializer));

            ChannelFuture channelFuture = b.connect(remotePeer);
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                if (channelFuture1.isSuccess()) {
                    log.debug("Successfully connect to remote server. remote peer = " + remotePeer);
                    RpcClientHandler handler = channelFuture1.channel().pipeline().get(RpcClientHandler.class);
                    addHandler(handler);
                }
            });
        });
    }

    private void addHandler(RpcClientHandler handler) {
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
    }

    private InetSocketAddress str2socket(String str) {
        if (str != null) {
            String[] parts = str.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            return new InetSocketAddress(host, port);
        }
        return null;
    }

    public RpcClientHandler chooseHandler(String serviceName) {
        List<String> services = serviceRegistry.discover(serviceName);
        if (services == null) {
            log.error("目前无服务", serviceName);
            return null;
        }

        // 使用配置的负载均衡策略
        String selected = loadBalancer.select(services);
        InetSocketAddress socketAddress = str2socket(selected);
        connectServerNode(socketAddress);

        // 获取连接
        return connectedServerNodes.get(socketAddress);
    }

}
