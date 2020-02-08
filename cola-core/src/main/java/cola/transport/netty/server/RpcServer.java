package cola.transport.netty.server;

import cola.common.RpcService;
import cola.executor.TaskExecutor;
import cola.registry.ServiceRegistry;
import cola.serialization.Serializer;
import cola.transport.netty.codec.RpcDecoder;
import cola.transport.netty.codec.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;


/**
 * @author lcf
 * NIO RPC 服务器
 */
@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean{
    /**
     * 服务器地址
     */
    private String serverAddress;

    /**
     * 引用服务注册器 启动后将自己的服务注册进去
     */
    private ServiceRegistry serviceRegistry;

    /**
     * 存放接口名和服务对象的映射关系
     */
    private Map<String, Object> handlerMap = new HashMap<>();

    /**
     *  序列化器
     */
    private Serializer serializer;

    /**
     * 服务handler线程池
     */
    private TaskExecutor taskExecutor;

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry, TaskExecutor taskExecutor) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
        this.taskExecutor = taskExecutor;
    }

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> services = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (!services.isEmpty()) {
            // 对于每个服务bean 注册到handlerMap 并且注册到注册中心
            for (Object service : services.values()) {
                // 因为可能被rpcService标注的类实现多个接口，所以我们获取真正的服务接口
                String name = service.getClass().getAnnotation(RpcService.class).value().getName();
                log.info("扫描并注册服务", name);
                handlerMap.put(name, service);

                // 注册到注册中心
                serviceRegistry.registry(name, serverAddress);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    /**
     * 服务器启动方法
     */
    public void start() throws InterruptedException {
        if (bossGroup == null && workerGroup == null) {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            // netty样板代码
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // TCP的粘包处理Handler
                            // 消息编解码时开始4个字节表示消息的长度
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                    .addLast(new RpcDecoder(serializer))
                                    .addLast(new RpcEncoder(serializer))
                                    .addLast(new RpcHandler(handlerMap, RpcServer.this));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 解析设置的服务器启动地址
            String[] parts = serverAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            log.info("服务器启动在{}端口", port);

//            future.channel().closeFuture().sync();
        }
    }

    /**
     * 提交任务到线程池
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
    }
}
