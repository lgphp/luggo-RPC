package ai.totok.infra.luggo.server;

import ai.totok.infra.luggo.annotation.LugooProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lgphp
 * @date 2020-03-15 11:17
 * @description
 */
@Component
@Slf4j
public class ServiceRegister {

    public static  ConcurrentHashMap<String ,   Object> serviceRegister = new ConcurrentHashMap<>();

    //  注册有@LugooService的服务
    public ServiceRegister(ApplicationContext applicationContext ,RpcServer rpcServer ) {
        //  获取所有注册在spring中的bean
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(LugooProvider.class);
        beansWithAnnotation.forEach((interfaceName,impl) ->{
            String interfaceConicalName = impl.getClass().getInterfaces()[0].getCanonicalName();
            rpcServer.pubServiceToRegister(interfaceConicalName,impl);
        });


    }
}
