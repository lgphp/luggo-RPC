package ai.totok.infra.luggo.annotation;


import java.lang.annotation.*;

/**
 * @author lgphp
 * @date 2020-03-19 13:10
 * @description
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface LugooConsumer {
    String servicename() default "";
}
