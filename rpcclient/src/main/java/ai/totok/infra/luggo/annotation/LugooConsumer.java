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

    // 负载均衡的实现类
    String loadbalance() default "RandomBalance";

    // 如果出错回退的方法
    String fallback() default "";

    // 请求超时时间 秒为单位
    int requestTimeOut() default 5;

}

