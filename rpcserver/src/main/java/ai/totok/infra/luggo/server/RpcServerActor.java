package ai.totok.infra.luggo.server;

import ai.totok.infra.luggo.core.RPCInvokeVO;
import ai.totok.infra.luggo.core.RPCReponseVO;
import akka.actor.UntypedAbstractActor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * @author lgphp
 * @date 2020-03-15 10:57
 * @description
 */
@Slf4j
public class RpcServerActor extends UntypedAbstractActor {
    @Override
    public void onReceive(Object message) throws Throwable {
        RPCReponseVO resp = new RPCReponseVO();

        if (message instanceof RPCInvokeVO) {
            RPCInvokeVO rpcInvokeVO = (RPCInvokeVO) message;
            //获取服务service
            try {
                log.info("start:[request_id:{}    service_name:{} ,   method:{}] ", rpcInvokeVO.getRequestId(), rpcInvokeVO.getServerName(), rpcInvokeVO.getMethodName());
                long t = Instant.now().toEpochMilli();
                Object bean = ServiceRegister.serviceRegister.get(rpcInvokeVO.getServerName());
                Method method = bean.getClass().getMethod(rpcInvokeVO.getMethodName(), rpcInvokeVO.getParamTypes());
                Object result = method.invoke(bean, rpcInvokeVO.getArgs());
                log.info("end:[request_id:{}   cost_time:{}ms]", rpcInvokeVO.getRequestId(), (Instant.now().toEpochMilli() - t));
                resp.setRequestId(rpcInvokeVO.getRequestId());
                resp.setRetObj(result);
                getSender().tell(resp, getSelf());
            } catch (Exception e) {
                resp.setRequestId(rpcInvokeVO.getRequestId());
                resp.setRetObj(e);
                getSender().tell(resp, getSelf());
            }

        } else {
            unhandled(message);
        }
    }


}
