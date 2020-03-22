package ai.totok.infra.luggo.core.loadbalance;

import akka.actor.ActorRef;
import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * @author lgphp
 * @date 2020-03-22 12:42
 * @description
 */
@Slf4j
public class RandomBalance implements ILoadBalance {
    @Override
    public ActorRef getRouterChannel(String serviceName, ArrayListMultimap<String, ActorRef> routerMap) {
        List<ActorRef> actorRefs = routerMap.get(serviceName);
        int size = actorRefs.size();
        return actorRefs.get(new Random().nextInt(size));
    }
}
