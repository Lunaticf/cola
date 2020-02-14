package cola.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author lunaticf
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ColaService {
    Class<?> value();
}
