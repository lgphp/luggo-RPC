package ai.totok.infra.luggo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lgphp
 * @date 2020-03-15 10:52
 * @description
 */
@Configuration
public class ClientConf {
 
    private  String clientAddr;

    private  int clientPort;

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

    public static void main(String[] args) throws Exception {
        System.out.println("InetAddress.getLocalHost().getHostAddress() = " + InetAddress.getLocalHost().getHostAddress());
    }
    

    @Value("${luggo.client.port:16611}")
    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    @Value("${luggo.client.addr:0.0.0.0}")
    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    public Map<String, ? extends Object> getMapConf() {
        Map<String, Object> m = new HashMap<>();
        m.put("akka.actor.provider" , "cluster");
        m.put("akka.actor.allow-java-serialization" , "on");
        m.put("akka.actor.warn-about-java-serializer-usage", "off");
        m.put("akka.remote.artery.transport" , "tcp");
        m.put("akka.remote.artery.canonical.port" ,  this.clientPort);
        try {
            m.put("akka.remote.artery.canonical.hostname", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        m.put("akka.remote.artery.enabled", "on");
        m.put("akka.remote.artery.advanced.maximum-frame-size", "256KiB");
        m.put("akka.remote.artery.advanced.buffer-pool-size", 128);
        m.put("akka.remote.artery.advanced.maximum-large-frame-size", "4MiB");
        m.put("akka.remote.artery.advanced.large-buffer-pool-size", 32);
        return m;
    }
}
