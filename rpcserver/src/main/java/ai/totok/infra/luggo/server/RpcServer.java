package ai.totok.infra.luggo.server;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.alibaba.fastjson.JSONObject;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author lgphp
 * @date 2020-03-15 10:59
 * @description
 */
@Component
@Slf4j
public final  class RpcServer {
    @Autowired
    ServerConf serverConf;


    private CuratorFramework registrrClient;

    public RpcServer(@Qualifier("zkClient") CuratorFramework registrrClient) {
        this.registrrClient = registrrClient;
    }

    @PostConstruct
    private void init(){
        startServer();
    }

    private  void startServer(){
        ActorSystem actorSystem = ActorSystem.create("luggo_rpcserver_sys_actor", ConfigFactory.parseMap(serverConf.getMapConf()));
        // 启动rpc处理的actor , 根据配置启动actor的数量
        IntStream.range(0,serverConf.getActorNum()).forEach(i->{
                actorSystem.actorOf(Props.create(RpcServerActor.class) , "luggo_rpc_handler_actor_"+i);
        });
    }

    // 手动发布服务
    public void  pubService(Map<String , Object> serviceMap){
        serviceMap.forEach((key, value) -> ServiceRegister.serviceRegister.put(key, value));
    }


    // 发布服务到zk
    @Deprecated
    private void pubServiceToRegister0(String serviceClassName , Object serviceImpl){
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(serviceImpl);
            outputStream.flush();
            /**
             * 服务的地址为 k: ip:port/serviceClassName  v
             */
            String servicePath = String.format("%s:%s/%s" , serverConf.getAddr() , serverConf.getPort() , serviceClassName);
            registrrClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/service/" + servicePath , byteArrayOutputStream.toByteArray());
            log.info("[{}] 注册成功", servicePath);
        } catch (  Exception e) {
            log.warn("注册服务失败:{}", serviceClassName, e);
        }

    }


    // 发布服务到zk
    public void pubServiceToRegister(String serviceClassName , Object serviceImpl){
        try {
            /**
             * 服务的地址为 k: {"addr": "ip:port", "srvname" : serviceClassName  n:actornum}
             */
            JSONObject serviceInfo = new JSONObject().fluentPut("srvaddr" , serverConf.getAddr()).fluentPut("port" , serverConf.getPort()).fluentPut("srvname" , serviceClassName ).fluentPut( "actnum" , serverConf.getActorNum());
            String servicePath = String.format("%s:%s:%s" , serverConf.getAddr() , serverConf.getPort() , serviceClassName);
            ServiceRegister.serviceRegister.put(serviceClassName, serviceImpl);
            registrrClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/service/" + servicePath, JSONObject.toJSONBytes(serviceInfo));
            log.info("[{}] 注册成功", servicePath);
        } catch (  Exception e) {
            log.warn("注册服务失败:{}", serviceClassName, e);
        }

    }



}
