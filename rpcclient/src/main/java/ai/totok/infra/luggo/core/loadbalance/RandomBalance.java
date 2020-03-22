package ai.totok.infra.luggo.core.loadbalance;

import akka.actor.ActorRef;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author lgphp
 * @date 2020-03-22 12:42
 * @description
 */
@Slf4j
public class RandomBalance implements ILoadBalance {
    @Override
    public ActorRef getRouterChannel(String serviceName, ArrayListMultimap<String, Map<String, ActorRef>> routerMap) {

        List<Map<String, ActorRef>> maps = routerMap.get(serviceName);
        int size = maps.size();
        List<ActorRef> routers = Lists.newArrayList();
        maps.forEach(v -> {
            ActorRef[] actorRefs = v.values().toArray(new ActorRef[0]);
            routers.add(actorRefs[0]);
        });
        return routers.get(new Random().nextInt(size));
    }
}
