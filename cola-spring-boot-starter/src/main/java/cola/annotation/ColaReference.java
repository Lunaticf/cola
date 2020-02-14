package cola.annotation;

import cola.common.enumeration.InvokeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lcf
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColaReference {
    InvokeType invokeType() default InvokeType.SYNC;
}
