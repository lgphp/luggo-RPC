package ai.totok.infra.luggo.client;

import ai.totok.infra.luggo.core.RPCInvokeVO;
import ai.totok.infra.luggo.core.RPCReponseVO;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.springframework.cglib.proxy.Proxy;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author lgphp
 * @date 2020-03-15 11:28
 * @description
 */

public class RemoteProxyFactory {


    public static  Object createService(Class serviceInterface,String srvaddr) {
        ActorRef router = RouterChannel.routerChannel.get(srvaddr);

        Object serviceImpl = Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[]{serviceInterface}, (o, method, args) -> {
            RPCInvokeVO rpcInvokeVO = new RPCInvokeVO();
            String requestId = UUID.randomUUID().toString();
            rpcInvokeVO.setRequestId(requestId);
            rpcInvokeVO.setServerName(serviceInterface.getName());
            rpcInvokeVO.setMethodName(method.getName());
            rpcInvokeVO.setParamTypes(method.getParameterTypes());
            rpcInvokeVO.setArgs(args);
            Future<Object> future = Patterns.ask(router, rpcInvokeVO, new Timeout(Duration.create(5, TimeUnit.SECONDS)));
            Object result = Await.result(future, Duration.create(5, TimeUnit.SECONDS));
            return ((RPCReponseVO) result).getRetObj();
        });
        return serviceImpl;
    }
}
