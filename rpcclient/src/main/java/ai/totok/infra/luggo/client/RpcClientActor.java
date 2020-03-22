package ai.totok.infra.luggo.client;

import akka.actor.UntypedAbstractActor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lgphp
 * @date 2020-03-15 11:26
 * @description
 */
@Slf4j
public class RpcClientActor  extends UntypedAbstractActor {
    private static RpcClientActor ctx;
    {
        ctx = this;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        log.info("message:{}", message);
        return;
    }

    public static RpcClientActor getCtx() {
        return ctx;
    }
}
