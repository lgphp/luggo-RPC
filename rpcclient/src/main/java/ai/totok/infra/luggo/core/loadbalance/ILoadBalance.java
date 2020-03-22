package ai.totok.infra.luggo.core.loadbalance;

import akka.actor.ActorRef;
import com.google.common.collect.ArrayListMultimap;

/**
 * @author lgphp
 * @date 2020-03-22 12:39
 * @description
 */
public interface ILoadBalance {
    ActorRef getRouterChannel(String serviceName, ArrayListMultimap<String, ActorRef> routerMap);
}
