package cola.autoconfig;

import cola.annotation.EnableCola;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author lcf
 * 解析EnableCola 注解 并初始化自动配置类
 */
public class ColaImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata am, BeanDefinitionRegistry bdr) {
        Map<String, Object> attributes = am.getAnnotationAttributes(EnableCola.class.getName());
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(ColaAutoConfiguration.class);

        // 解析EnableCola server 和 client
        if (attributes != null && attributes.containsKey("server")) {
            bdb.addPropertyValue("server", attributes.get("server"));
        }

        if (attributes != null && attributes.containsKey("client")) {
            bdb.addPropertyValue("client", attributes.get("client"));
        }
        bdr.registerBeanDefinition(ColaAutoConfiguration.class.getName(), bdb.getBeanDefinition());
    }
}
