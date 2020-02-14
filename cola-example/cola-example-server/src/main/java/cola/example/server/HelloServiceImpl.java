package cola.example.server;

import cola.annotation.ColaService;
import cola.example.common.HelloService;

/**
 * @author lcf
 * @date 2020-02-14 16:39
 */
@ColaService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello ! " + name;
    }
}
