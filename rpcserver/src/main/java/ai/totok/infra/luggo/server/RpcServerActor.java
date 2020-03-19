package ai.totok.infra.luggo.server;

import ai.totok.infra.luggo.core.RPCInvokeVO;
import ai.totok.infra.luggo.core.RPCReponseVO;
import akka.actor.UntypedAbstractActor;

import java.lang.reflect.Method;

/**
 * @author lgphp
 * @date 2020-03-15 10:57
 * @description
 */
public class RpcServerActor extends UntypedAbstractActor {
    @Override
    public void onReceive(Object message) throws Throwable {
        RPCReponseVO resp = new RPCReponseVO();
        if (message instanceof RPCInvokeVO){
            RPCInvokeVO rpcInvokeVO = (RPCInvokeVO) message;
            //获取服务service
            try {
                Object bean = ServiceRegister.serviceRegister.get(rpcInvokeVO.getServerName());
                Method method = bean.getClass().getMethod(rpcInvokeVO.getMethodName() , rpcInvokeVO.getParamTypes());
                Object result = method.invoke(bean , rpcInvokeVO.getArgs());
                resp.setRequestId(rpcInvokeVO.getRequestId());
                resp.setRetObj(result);
                getSender().tell(resp , getSelf());
            }catch (Exception e){
                resp.setRequestId(rpcInvokeVO.getRequestId());
                resp.setRetObj(e);
                getSender().tell(resp , getSelf());
            }

        }else{
            unhandled(message);
        }
    }
}
