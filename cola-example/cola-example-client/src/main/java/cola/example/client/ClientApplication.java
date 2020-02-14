package cola.example.client;

import cola.annotation.ColaReference;
import cola.annotation.EnableCola;
import cola.common.enumeration.InvokeType;
import cola.example.common.HelloService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lcf
 */
@SpringBootApplication
@EnableCola(client = true)
public class ClientApplication implements CommandLineRunner {

   @ColaReference(invokeType = InvokeType.SYNC)
   HelloService helloService;

   public static void main(String[] args) {
      SpringApplication.run(ClientApplication.class, args);
   }


   @Override
   public void run(String... args) throws Exception {
      System.out.println(helloService.hello("lcf"));
   }
}
