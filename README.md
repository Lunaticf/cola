# cola
A light rpc framework. 

Grab a cola and drink it, out of the box.
(mainly due to a cats' name :),  variable naming is the hardest part aha)

Just for learning, the implementation is naive, refactoring.

## Features
- Support multiple protocol such as JDK, Protostuff, hessian, JSON(fastjson), kyro.
- Support features like load-balance(random, Round-Robin, Least-active, Consistent Hash).
- Support service discovery services like ZooKeeper.
- Support oneway, synchronous, asynchronous invoking and callback.
- Easy integrated with provided Spring boot starter

### Requirements
The minimum requirements to run the quick start are:
* JDK 1.8 or above


### Quick Start

#### ZooKeeper 
start Zookeeper, docker example.
```shell
docker run --privileged=true -d --name zookeeper --publish 2181:2181  -d zookeeper:latest
```

#### import dependency

```
<dependency>
    <groupId>com.lunaticf</groupId>
    <artifactId>cola-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

you can also use it without spring boot, see test for example.

#### configuration
```yml
// client
cola:
  loadbalance: random
  register: 127.0.0.1:2181
  serializer: hessian

// server
cola:
  register: 127.0.0.1:2181
  serializer: hessian
  server: 127.0.0.1:8888
  threads: 10
```

#### define service
```java
public interface HelloService {
    String hello(String name);
}
```

#### server
```java
// impl
@ColaService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello ! " + name;
    }
}
```

#### client
```java
@ColaReference(invokeType = InvokeType.AYSNC)
HelloService helloService;

// invoke
helloService.hello("lcf");
RPCFuture future = RPCContext.getContext().getFuture();

// add callback
future.addCallback(response -> {
 if (response.hasError()) {
    System.out.println("请求发送失败");
 } else {
    System.out.println("请求发送成功");
 }
});

System.out.println(future.get());
```



please refer to cola-example for detail.








