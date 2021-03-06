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

        return Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[]{serviceInterface}, (proxy, method, args) -> {
            long st = System.currentTimeMillis();
            ActorRef router = loadBalance.getRouterChannel(serviceInterface.getCanonicalName(), RouterChannel.routerChannel);
            RPCInvokeVO rpcInvokeVO = new RPCInvokeVO();
            String requestId = UUID.randomUUID().toString();
            rpcInvokeVO.setRequestId(requestId);
            rpcInvokeVO.setServerName(serviceInterface.getName());
            rpcInvokeVO.setMethodName(method.getName());
            rpcInvokeVO.setParamTypes(method.getParameterTypes());
            rpcInvokeVO.setArgs(args);
            Future<Object> future = Patterns.ask(router, rpcInvokeVO, new Timeout(Duration.create(requestTimeout, TimeUnit.SECONDS)));
            try {
                Object result = Await.result(future, Duration.create(requestTimeout, TimeUnit.SECONDS));
                Object retObj = ((RPCReponseVO) result).getRetObj();
                if (retObj instanceof Exception) {
                    // fallback ??????
                    return doFallback(serviceBean, fallbackMethod);
                }
                log.info("[requestId:{}]  router:[{}]  cost:[{} ms]", requestId, router, System.currentTimeMillis() - st);
                return retObj;
            } catch (TimeoutException te) {
                log.warn("invoke remote method timeout [requestId:{}]", requestId);
                te.printStackTrace();
            } catch (Exception e) {
                log.error("invoke remote method error: [requestId:{}] ", requestId);
                e.printStackTrace();
            }
            return null;
        });
    }


    private static Object doFallback(Object serviceBean, String fallbackMethod) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (StringUtils.isNotEmpty(fallbackMethod)) {
            Method fallInvoke = serviceBean.getClass().getMethod(fallbackMethod);
            fallInvoke.setAccessible(true);
            return fallInvoke.invoke(serviceBean);
        }
        throw new InternalError("????????????");

    }
}
