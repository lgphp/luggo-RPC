package ai.totok.infra.luggo.client;

import akka.actor.ActorRef;
import akka.routing.RoundRobinGroup;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author lgphp
 * @date 2020-03-19 17:58
 * @description
 */
public class RouterChannel {
    public static ConcurrentHashMap<String ,ActorRef> routerChannel = new ConcurrentHashMap();

    public static void createRouter(String srvAddr , int port , int actorNum){
        List<String> routeePath = IntStream.range(0, actorNum).mapToObj(i -> "akka://luggo_rpcserver_sys_actor@" + srvAddr + ":" + port + "/user/luggo_rpc_handler_actor_" + i).collect(Collectors.toList());
        ActorRef router = RpcClientActor.getCtx().getContext().actorOf(new RoundRobinGroup(routeePath).props(), "router");
        if (Objects.nonNull(router)) {
            routerChannel.put(srvAddr+":"+port , router);
        }
    }

}
