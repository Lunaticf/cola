package cola.transport.netty.server;

import cola.common.RpcRequest;
import cola.common.RpcResponse;
import cola.executor.TaskExecutor;
import cola.registry.ServiceRegistry;
import cola.serialization.Serializer;
import cola.transport.netty.codec.RpcDecoder;
import cola.transport.netty.codec.RpcEncoder;
import cola.transport.netty.server.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author lcf
 * NIO RPC 服务器
 */
@Slf4j
public class RpcServer {
    /**
     * 服务器地址
     */
    private String serverAddress;

    /**
     * 引用服务注册器 启动后将自己的服务注册进去
     */
    private ServiceRegistry serviceRegistry;

    /**
     *  序列化器
     */
    private Serializer serializer;

    /**
     * 服务handler线程池
     */
    private TaskExecutor taskExecutor;

    /**
     * 存放接口名和服务对象的映射关系 HelloService -> helloServiceImpl实例
     */
    private Map<String, Object> handlerMap = new ConcurrentHashMap<>();

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    /**
     * Constructor
     */
    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry, Serializer serializer, TaskExecutor taskExecutor) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 服务器启动方法
     */
    public void start() throws InterruptedException {
        if (bossGroup == null && workerGroup == null) {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            log.info("服务器开始启动");

            // netty样板代码
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // TCP的粘包处理Handler
                            // 消息编解码时开始4个字节表示消息的长度
                            ch.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class, serializer))
                                    .addLast(new RpcEncoder(RpcResponse.class, serializer))
                                    .addLast(new RpcRequestHandler(handlerMap, RpcServer.this));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 解析设置的服务器启动地址
            String[] parts = serverAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();

            log.info("服务器启动在{}端口", port);

            future.channel().closeFuture().sync();
        }
    }

    /**
     * 注册服务
     */
    public void registerService(String service, Object serviceBean) {
        handlerMap.put(service, serviceBean);

        // 注册到注册中心
        serviceRegistry.registry(service, serverAddress);
        log.info("注册服务: {} 地址: {}", service, serverAddress);
    }

    /**
     * 提交RPC请求任务到线程池去完成任务
     */
    public void handleRpcExecute(Runnable runnable) {
        taskExecutor.submit(runnable);
    }

    /**
     * 服务器关闭
     */
    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("服务器关闭");
    }
}
