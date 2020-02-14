package cola.example.server;

import cola.annotation.EnableCola;
import cola.transport.netty.server.RPCServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lcf
 */
@SpringBootApplication
@EnableCola(server = true)
public class ServerApplication implements CommandLineRunner {

    @Autowired
    private RPCServer rpcServer;

    public static void main(String... args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        rpcServer.start();
    }
}
