# cola
A light rpc framework. 

Grab a cola and drink it, out of the box.
(mainly due to a cats' name :),  variable naming is the hardest part aha)

Simple: easy to learn, easy to develop, easy to integrate and easy to deploy

## Features
- Support multiple protocol such as JDK, hessian, JSON(fastjson), kyro.
- Support thread executor like ThreadPoolExecutor, disruptor.
- Support advanced features like load-balance(random, Round-Robin), HA strategy(Failfast, Failover).
- Support service discovery services like ZooKeeper or Consul.
- Support synchronous or asynchronous invoking and callback.
- Easy integrated with Spring boot starter

### Requirements
The minimum requirements to run the quick start are:
* JDK 1.8 or above


### Quick Start

### Reference
https://gitee.com/huangyong/rpc
https://github.com/luxiaoxun/NettyRpc
https://github.com/pyloque/rpckids


#### cola.serialization 数据序列化层
序列化就是将Java对象转化为字节的过程，然后就能传输和保存了。其实转化为JSON字符串或者是用其他的形式保存也是序列化，
总的来说只是将一个事物转化为另一种表示形式，然后能够复原。

序列化协议的选型指标：

- 通用性 是否只能用于java间序列化/反序列化，是否跨语言，跨平台。
- 性能 空间和时间开销，序列化后的数据大小和序列化和反序列的开销。

Java序列化的时候通常要注意：

- 静态类变量自然不会被序列化 序列化保存的是对象的状态
- Transient 属性不会被序列化
- serialVersionUID 判断类的serialVersionUID来验证的版本一致

RPC框架通常支持可插拔的多种序列化方式，这里支持：

- JDK原生序列化 性能差
- Hessian 产生码流小 跨语言
- JSON 跨语言





可参考dubbo提供的各种协议的性能测试
http://dubbo.apache.org/zh-cn/docs/user/perf-test.html

- config 配置层：对外配置接口，以 ServiceConfig, ReferenceConfig 为中心，可以直接初始化配置类，也可以通过 spring 解析配置生成配置类
- proxy 服务代理层：服务接口透明代理，生成服务的客户端 Stub 和服务器端 Skeleton, 以 ServiceProxy 为中心，扩展接口为 ProxyFactory
- registry 注册中心层：封装服务地址的注册与发现，以服务 URL 为中心，扩展接口为 RegistryFactory, Registry, RegistryService
- cluster 路由层：封装多个提供者的路由及负载均衡，并桥接注册中心，以 Invoker 为中心，扩展接口为 Cluster, Directory, Router, LoadBalance
- monitor 监控层：RPC 调用次数和调用时间监控，以 Statistics 为中心，扩展接口为 MonitorFactory, Monitor, MonitorService
- protocol 远程调用层：封装 RPC 调用，以 Invocation, Result 为中心，扩展接口为 Protocol, Invoker, Exporter
- exchange 信息交换层：封装请求响应模式，同步转异步，以 Request, Response 为中心，扩展接口为 Exchanger, ExchangeChannel, ExchangeClient, ExchangeServer
- transport 网络传输层：抽象 mina 和 netty 为统一接口，以 Message 为中心，扩展接口为 Channel, Transporter, Client, Server, Codec
- cola.serialization 数据序列化层：可复用的一些工具，扩展接口为 Serialization, ObjectInput, ObjectOutput, ThreadPool






docker run --privileged=true -d --name zookeeper --publish 2181:2181  -d zookeeper:latest
docker exec -it 338838f5fb66 /bin/bash
./zkcli.sh
ls

https://blog.csdn.net/qq_20641565/article/details/78797975
https://www.shadowwu.club/2018/10/23/reflect_int/index.html

暂时只支持包装类型
后续通过cglib生成异步代理类


轻量级 不使用Spring做依赖

