package ai.totok.infra.luggo.client;

import akka.actor.ActorRef;
import akka.routing.RoundRobinGroup;
import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * @author lgphp
 * @date 2020-03-19 17:58
 * @description
 */
@Slf4j
public class RouterChannel {
    // 一个服务对应多个router actor
    public static ArrayListMultimap<String, ActorRef> routerChannel = ArrayListMultimap.create();

    public static void createRouter(String srvAddr, int port, int actorNum, String serviceName) {
        List<String> routeePath = IntStream.range(0, actorNum).mapToObj(i -> "akka://luggo_rpcserver_sys_actor@" + srvAddr + ":" + port + "/user/luggo_rpc_handler_actor_" + i).collect(Collectors.toList());
        ActorRef router = RpcClientActor.getCtx().getContext().actorOf(new RoundRobinGroup(routeePath).props(), "router_" + UUID.randomUUID().toString());
        if (Objects.nonNull(router)) {

            routerChannel.put(serviceName, router);
        }
    }

}
