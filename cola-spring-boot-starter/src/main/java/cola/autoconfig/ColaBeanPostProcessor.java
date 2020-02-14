package cola.autoconfig;

import cola.annotation.ColaReference;
import cola.annotation.ColaService;
import cola.common.enumeration.InvokeType;
import cola.transport.netty.client.RPCClient;
import cola.transport.netty.server.RPCServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;

/**
 * @author lcf
 */
@Slf4j
public class ColaBeanPostProcessor implements BeanPostProcessor {

    private RPCClient rpcClient;
    private RPCServer rpcServer;


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 如果是服务注解
        if (rpcServer != null && bean != null && bean.getClass().isAnnotationPresent(ColaService.class)) {
            Class<?> value = bean.getClass().getAnnotation(ColaService.class).value();
            rpcServer.registerService(value.getName(), bean);
            log.info("注册服务，{}", value.getName());
        }


        // 如果某个bean 的field 有 引用服务注解
        if (rpcClient != null && bean != null) {
            Class clazz = ClassUtils.getUserClass(bean);
            Field[] fields = clazz.getDeclaredFields();
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    if (field.isAnnotationPresent(ColaReference.class)) {
                        InvokeType invokeType = field.getAnnotation(ColaReference.class).invokeType();
                        Object obj = rpcClient.create(field.getType(), invokeType);
                        try {
                            field.setAccessible(true);
                            field.set(bean, obj);
                        } catch (IllegalArgumentException | IllegalAccessException ex) {
                            log.error(ex.getMessage());
                            throw new BeansException(ex.getMessage()) {
                            };
                        }
                        log.info("service reference beanName = {}, field = {}", beanName, field.getName());
                    }
                }
            }
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }


    public ColaBeanPostProcessor(RPCClient rpcClient, RPCServer rpcServer) {
        this.rpcClient = rpcClient;
        this.rpcServer = rpcServer;
    }
}
