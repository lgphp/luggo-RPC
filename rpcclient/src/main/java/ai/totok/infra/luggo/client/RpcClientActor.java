package ai.totok.infra.luggo.client;

import akka.actor.UntypedAbstractActor;

/**
 * @author lgphp
 * @date 2020-03-15 11:26
 * @description
 */
public class RpcClientActor  extends UntypedAbstractActor {
    private static RpcClientActor ctx;
    {
        ctx = this;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        return;
    }

    public static RpcClientActor getCtx() {
        return ctx;
    }
}
