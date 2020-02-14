package cola.autoconfig;

import cola.cluster.LoadBalancer;
import cola.cluster.loadbalancer.ConsistentHashLoadBalancer;
import cola.cluster.loadbalancer.LeastActiveLoadBalancer;
import cola.cluster.loadbalancer.RandomLoadBalancer;
import cola.cluster.loadbalancer.RoundRobinLoadBalancer;
import cola.executor.TaskExecutor;
import cola.executor.jdk.ThreadPoolTaskExecutor;
import cola.filter.impl.ActiveCountAfterFilter;
import cola.filter.impl.ActiveCountBeforeFilter;
import cola.registry.ServiceRegistry;
import cola.registry.zookeeper.ZkServiceRegistry;
import cola.serialization.Serializer;
import cola.serialization.hessian.HessianSerializer;
import cola.serialization.jdk.JdkSerializer;
import cola.serialization.json.JsonSerializer;
import cola.serialization.kyro.KyroSeralizer;
import cola.serialization.protostuff.ProtostuffSerializer;
import cola.transport.netty.client.RPCClient;
import cola.transport.netty.server.RPCServer;
import io.protostuff.Rpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author lcf
 */
@Slf4j
@Configuration
public class ColaAutoConfiguration implements EnvironmentAware, DisposableBean, BeanFactoryPostProcessor, ApplicationContextAware {

    @Autowired
    private ApplicationContext applicationContext;

    private Environment environment;
    private Boolean server;
    private Boolean client;
    private ColaBeanPostProcessor colaBeanPostProcessor;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 服务端角色
     *
     * @param server 状态
     */
    public void setServer(Boolean server) {
        this.server = server;
    }

    /**
     * 客户端角色
     *
     * @param client 状态
     */
    public void setClient(Boolean client) {
        this.client = client;
    }

    @Override
    public void destroy() throws Exception {
    }

    /**
     * 后置组件处理工厂
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        if (server || client) {
            try {
                // 添加后置组件处理
                beanFactory.addBeanPostProcessor(colaBeanPostProcessor());
            } catch (Throwable e) {
                throw new BeansException(e.getMessage()) {
                };
            }
        }
    }


    @Bean(name = "ch")
    public ConsistentHashLoadBalancer consistentHashLoadBalancer() {
        return new ConsistentHashLoadBalancer();
    }

    @Bean(name = "random")
    public RandomLoadBalancer randomLoadBalancer() {
        return new RandomLoadBalancer();
    }

    @Bean(name = "rr")
    public RoundRobinLoadBalancer roundRobinLoadBalancer() {
        return new RoundRobinLoadBalancer();
    }

    @Bean(name = "la")
    public LeastActiveLoadBalancer leastActiveLoadBalancer() {
        return new LeastActiveLoadBalancer();
    }

    @Bean(name = "protostuff")
    public ProtostuffSerializer protostuffSerializer() {
        return new ProtostuffSerializer();
    }

    @Bean(name = "hessian")
    public HessianSerializer hessianSerializer() {
        return new HessianSerializer();
    }

    @Bean(name = "kyro")
    public KyroSeralizer kyroSeralizer() {
        return new KyroSeralizer();
    }

    @Bean(name = "json")
    public JsonSerializer jsonSerializer() {
        return new JsonSerializer();
    }

    @Bean(name = "jdk")
    public JdkSerializer jdkSerializer() {
        return new JdkSerializer();
    }

    @Bean
    public ServiceRegistry serviceRegistry() {
        return new ZkServiceRegistry(environment.getProperty("cola.register"));
    }

    @Bean
    public RPCClient rpcClient() {
        if (client) {
            LoadBalancer loadBalancer = applicationContext.getBean(environment.getProperty("cola.loadbalance"), LoadBalancer.class);
            Serializer serializer = applicationContext.getBean(environment.getProperty("cola.serializer"), Serializer.class);
            ServiceRegistry serviceRegistry = applicationContext.getBean(ServiceRegistry.class);

            RPCClient rpcClient = new RPCClient(serializer, serviceRegistry, loadBalancer);

            if (loadBalancer instanceof LeastActiveLoadBalancer) {
                rpcClient.addBeforeFilter(new ActiveCountBeforeFilter());
                rpcClient.addAfterFilter(new ActiveCountAfterFilter());
            }

            System.out.println("fuck");

            return rpcClient;
        }
        return null;
    }

    @Bean
    public RPCServer rpcServer() {
        if (server) {
            System.out.println(applicationContext);
            System.out.println(environment.getProperty("cola.serializer"));
            Serializer serializer = applicationContext.getBean(environment.getProperty("cola.serializer"), Serializer.class);
            ServiceRegistry serviceRegistry = applicationContext.getBean(ServiceRegistry.class);
            String serverAddress = environment.getProperty("cola.server");
            TaskExecutor taskExecutor = new ThreadPoolTaskExecutor(Integer.parseInt(environment.getProperty("cola.threads")));

            RPCServer rpcServer = new RPCServer(serverAddress, serviceRegistry, serializer, taskExecutor);
            return rpcServer;
        }
        return null;
    }

    /**
     * 自动扫描，注册服务组件
     *
     * @return 注册服务组件
     * @throws java.lang.Throwable 异常
     */
    @Bean
    public ColaBeanPostProcessor colaBeanPostProcessor() throws Throwable {
        if (colaBeanPostProcessor != null) {
            return colaBeanPostProcessor;
        }

        RPCClient rpcClient = null;
        RPCServer rpcServer = null;

        if (server) {
            rpcServer = applicationContext.getBean(RPCServer.class);
        }

        if (client) {
            rpcClient = applicationContext.getBean(RPCClient.class);
        }


        colaBeanPostProcessor = new ColaBeanPostProcessor(rpcClient, rpcServer);
        return colaBeanPostProcessor;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
