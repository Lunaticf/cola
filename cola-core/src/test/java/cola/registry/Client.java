package cola.registry;

import cola.registry.zookeeper.ZkServiceRegistry;

/**
 * @author lcf
 */
public class Client {
    public static void main(String[] args) throws InterruptedException {
        ZkServiceRegistry registry = new ZkServiceRegistry("127.0.0.1:2181");

        for (int i = 0; i < 100; i++) {
            System.out.println(registry.discover("com.cola.HelloService"));
            Thread.sleep(2000);
        }
    }
}
