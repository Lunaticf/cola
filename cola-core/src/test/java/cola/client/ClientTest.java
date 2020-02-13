package cola.client;

import cola.cluster.LoadBalancer;
import cola.cluster.loadbalancer.RandomLoadBalancer;
import cola.common.HelloService;
import cola.common.RpcCallback;
import cola.common.RpcFuture;
import cola.common.RpcResponse;
import cola.common.context.RpcContext;
import cola.common.enumeration.InvokeType;
import cola.registry.ServiceRegistry;
import cola.registry.zookeeper.ZkServiceRegistry;
import cola.serialization.Serializer;
import cola.serialization.jdk.JdkSerializer;
import cola.transport.netty.client.RpcClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author lcf
 */
public class ClientTest {

    String serverAddress;
    ServiceRegistry serviceRegistry;
    Serializer serializer;
    LoadBalancer loadBalancer;
    RpcClient rpcClient;

    @Before
    public void before() {
        serverAddress = "localhost:18866";
        serviceRegistry = new ZkServiceRegistry("127.0.0.1:2181");
        serializer = new JdkSerializer();
        loadBalancer = new RandomLoadBalancer();
        rpcClient = new RpcClient(serializer, serviceRegistry, loadBalancer);
    }

    @Test
    public void testSync() {
        HelloService helloService = rpcClient.create(HelloService.class);

        Assert.assertEquals("hello! lcf", helloService.hello("lcf"));
    }

    @Test
    public void testAsync() throws ExecutionException, InterruptedException {
        HelloService helloService = rpcClient.create(HelloService.class, InvokeType.AYSNC);
        helloService.hello("lcf");
        Future future = RpcContext.getContext().getFuture();


        Assert.assertEquals("hello! lcf",future.get());
    }

    @Test
    public void testCallback() throws ExecutionException, InterruptedException {
        HelloService helloService = rpcClient.create(HelloService.class, InvokeType.AYSNC);
        helloService.hello("lcf");
        RpcFuture rpcFuture = RpcContext.getContext().getFuture();
        rpcFuture.addCallback((response)->{
            if (response.hasError()) {
                System.out.println("请求错误");
            } else {
                System.out.println("请求成功");
            }
        });

        Assert.assertEquals("hello! lcf", rpcFuture.get());
    }
}