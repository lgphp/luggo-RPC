package ai.totok.infra.luggo.client;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author lgphp
 * @date 2020-03-15 11:39
 * @description
 */
@Component
public final class RpcClient {

    @Autowired
    ClientConf clientConf;
    @PostConstruct
    private void init(){
        startClient();
    }
    private void startClient(){
        ActorSystem actorSystem = ActorSystem.create("luggo_rpcclient_sys_actor" , ConfigFactory.parseMap(clientConf.getMapConf()));
        actorSystem.actorOf(Props.create(RpcClientActor.class),"luggo_rpcclient_handler_actor");
    }

}
