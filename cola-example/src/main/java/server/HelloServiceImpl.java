package server;

import cola.common.RpcService;
import common.HelloService;

/**
 * @author lcf
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello! " + name;
    }
}
