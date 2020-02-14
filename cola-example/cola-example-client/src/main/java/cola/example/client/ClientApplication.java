package cola.example.client;

import cola.annotation.ColaReference;
import cola.annotation.EnableCola;
import cola.common.RPCFuture;
import cola.common.context.RPCContext;
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

   @ColaReference(invokeType = InvokeType.AYSNC)
   HelloService helloService;

   public static void main(String[] args) {
      SpringApplication.run(ClientApplication.class, args);
   }


   @Override
   public void run(String... args) throws Exception {
         helloService.hello("lcf");
      RPCFuture future = RPCContext.getContext().getFuture();
      future.addCallback(response -> {
         if (response.hasError()) {
            System.out.println("请求发送失败");
         } else {
            System.out.println("请求发送成功");
         }
      });
      System.out.println(future.get());
   }
}
