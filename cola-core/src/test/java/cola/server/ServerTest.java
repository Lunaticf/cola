package cola.server;

import cola.common.HelloService;
import cola.common.HelloServiceImpl;
import cola.executor.TaskExecutor;
import cola.executor.jdk.ThreadPoolTaskExecutor;
import cola.registry.ServiceRegistry;
import cola.registry.zookeeper.ZkServiceRegistry;
import cola.serialization.Serializer;
import cola.serialization.hessian.HessianSerializer;
import cola.serialization.jdk.JdkSerializer;
import cola.transport.netty.server.RpcServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lcf
 */
@Slf4j
public class ServerTest {
    public static void main(String[] args) {

        String serverAddress = "127.0.0.1:18866";
        ServiceRegistry serviceRegistry = new ZkServiceRegistry("127.0.0.1:2181");
        Serializer serializer = new JdkSerializer();
        TaskExecutor taskExecutor = new ThreadPoolTaskExecutor(10);

        RpcServer rpcServer = new RpcServer(serverAddress, serviceRegistry, serializer, taskExecutor);
        HelloService helloService = new HelloServiceImpl();


        rpcServer.registerService(
                HelloService.class.getName(),
                helloService
        );

        try {
            rpcServer.start();
        } catch (Exception ex) {
            log.error("Exception: {}", ex);
        }
    }
}
