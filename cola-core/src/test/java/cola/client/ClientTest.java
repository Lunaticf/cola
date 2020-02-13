package cola.client;

import cola.cluster.LoadBalancer;
import cola.cluster.loadbalancer.RandomLoadBalancer;
import cola.common.HelloService;
import cola.common.RpcCallback;
import cola.common.RpcFuture;
import cola.common.RpcResponse;
import cola.registry.ServiceRegistry;
import cola.registry.zookeeper.ZkServiceRegistry;
import cola.serialization.Serializer;
import cola.serialization.jdk.JdkSerializer;
import cola.transport.netty.client.RpcClient;

/**
 * @author lcf
 */
public class ClientTest {
    public static void main(String[] args) throws Exception {
        String serverAddress = "localhost:18866";
        ServiceRegistry serviceRegistry = new ZkServiceRegistry("127.0.0.1:2181");
        Serializer serializer = new JdkSerializer();
        LoadBalancer loadBalancer = new RandomLoadBalancer();
        RpcClient rpcClient = new RpcClient(serializer, serviceRegistry, loadBalancer);

        HelloService helloService = rpcClient.create(HelloService.class);

        System.out.println(helloService.hello("lcf"));

        RpcClient.AsyncProxy asyn = rpcClient.createAsyn(HelloService.class);
        RpcFuture call = asyn.call("hello", "ouyang");
        call.addCallback(response -> {
            if (response.hasError()) {
                System.out.println("错误");
            } else {
                System.out.println("成功");
            }
        });
        System.out.println(call.get());
    }
}
