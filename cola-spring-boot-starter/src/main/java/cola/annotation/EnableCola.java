package cola.annotation;

import cola.autoconfig.ColaImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author lunaticf
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ColaImportBeanDefinitionRegistrar.class})
public @interface EnableCola {

    boolean server() default false;

    boolean client() default false;
}
