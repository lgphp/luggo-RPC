package ai.totok.infra.luggo.client;

import ai.totok.infra.luggo.core.RPCInvokeVO;
import ai.totok.infra.luggo.core.RPCReponseVO;
import ai.totok.infra.luggo.core.loadbalance.ILoadBalance;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cglib.proxy.Proxy;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author lgphp
 * @date 2020-03-15 11:28
 * @description
 */
@Slf4j
public class RemoteProxyFactory {


    public static Object createService(ILoadBalance loadBalance, Class serviceInterface, Object serviceBean, String fallbackMethod, int requestTimeout) {

        Object serviceImpl = Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[]{serviceInterface}, (o, method, args) -> {
            ActorRef router = loadBalance.getRouterChannel(serviceInterface.getSimpleName(), RouterChannel.routerChannel);
            RPCInvokeVO rpcInvokeVO = new RPCInvokeVO();
            String requestId = UUID.randomUUID().toString();
            rpcInvokeVO.setRequestId(requestId);
            rpcInvokeVO.setServerName(serviceInterface.getName());
            rpcInvokeVO.setMethodName(method.getName());
            rpcInvokeVO.setParamTypes(method.getParameterTypes());
            rpcInvokeVO.setArgs(args);
            Future<Object> future = Patterns.ask(router, rpcInvokeVO, new Timeout(Duration.create(requestTimeout, TimeUnit.SECONDS)));
            log.info("send:[request_id:{}]  router:{}", requestId, router);
            try {
                Object result = Await.result(future, Duration.create(requestTimeout, TimeUnit.SECONDS));
                Object retObj = ((RPCReponseVO) result).getRetObj();
                if (retObj instanceof Exception) {
                    // fallback 逻辑
                    return doFallback(serviceBean, fallbackMethod);
                }
                return retObj;
            } catch (TimeoutException te) {
                return doFallback(serviceBean, fallbackMethod);
            } catch (Exception e) {
                log.error(e.getClass().getName());
                // 其他错误，直接将异常传给业务端
                return e;
            }
        });
        return serviceImpl;
    }


    private static Object doFallback(Object serviceBean, String fallbackMethod) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (StringUtils.isNotEmpty(fallbackMethod)) {
            Method fallInvoke = serviceBean.getClass().getMethod(fallbackMethod);
            return fallInvoke.invoke(serviceBean);
        }
        throw new InternalError("服务异常");

    }
}
