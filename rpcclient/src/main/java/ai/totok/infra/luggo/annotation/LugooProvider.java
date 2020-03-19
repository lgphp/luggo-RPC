package ai.totok.infra.luggo.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author lgphp
 * @date 2020-03-19 13:10
 * @description
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface LugooProvider {
    String servicename() default "";
}
