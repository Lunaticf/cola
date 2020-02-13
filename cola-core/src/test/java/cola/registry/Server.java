package cola.registry;

import cola.registry.zookeeper.ZkServiceRegistry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lcf
 */
public class Server {
    public static void main(String[] args) throws InterruptedException {
        ZkServiceRegistry registry = new ZkServiceRegistry("127.0.0.1:2181");

        ExecutorService executorService = Executors.newCachedThreadPool();

        final AtomicInteger atomicInteger = new AtomicInteger(1);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> registry.registry("com.cola.HelloService", "127.0.0.1:" + atomicInteger.incrementAndGet()));
            Thread.sleep(1000);
        }
    }
}
