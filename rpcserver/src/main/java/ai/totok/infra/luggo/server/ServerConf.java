package ai.totok.infra.luggo.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lgphp
 * @date 2020-03-15 10:51
 * @description
 */

@Configuration
public class ServerConf {
    private String addr;
    private int port;
    private int actorNum;



    private String zkAddress;
    private String group;


    @Value("${luggo.register.addr:127.0.0.1}")
    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }
    @Value("${luggo.register.group:service}")
    public void setGroup(String group) {
        this.group = group;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public String getGroup() {
        return group;
    }

    public String getAddr() {
        return addr;
    }

    @Value("${luggo.server.addr:0.0.0.0}")
    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }
    @Value("${luggo.server.port:16610}")
    public void setPort(int port) {
        this.port = port;
    }

    public int getActorNum() {
        return actorNum;
    }

    @Value("${luggo.server.actor.num:100}")
    public void setActorNum(int actorNum) {
        this.actorNum = actorNum;
    }

    public Map<String, ? extends Object> getMapConf() {
        Map<String, Object> m = new HashMap<>();
        m.put("akka.actor.provider" , "cluster");
        m.put("akka.actor.allow-java-serialization" , "on");
        m.put("akka.actor.warn-about-java-serializer-usage", "off");
        m.put("akka.remote.artery.transport" , "tcp");
        m.put("akka.remote.artery.canonical.port" , this.port);
        try {
            m.put("akka.remote.artery.canonical.hostname", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return m;
    }

    public String getLocalAddr() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.getAddr();
    }
}
