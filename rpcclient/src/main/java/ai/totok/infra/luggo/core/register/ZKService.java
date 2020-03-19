package ai.totok.infra.luggo.core.register;

import ai.totok.infra.luggo.client.ClientConf;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.utils.CloseableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author lgphp
 * @date 2020-03-19 11:36
 * @description
 */
@Component
@Slf4j
public class ZKService {
    @Autowired
    ClientConf clientConf;
    private CuratorFramework client;

    @Bean(name = "zk")
    @Qualifier("zkClient")
    public CuratorFramework getRegisterClient(){
        RetryPolicy retryPolicy = new RetryForever(1000);
        try {
            client = CuratorFrameworkFactory.builder().connectString(clientConf.getZkAddress())
                    .sessionTimeoutMs(5000)
                    .connectionTimeoutMs(3000)
                    .retryPolicy(retryPolicy)
                    .namespace("luggo_" + clientConf.getGroup()).build();
            client.start();
        }catch (Exception e){
            log.error("zookeeper can not connect:{}" , e.getMessage());
            e.printStackTrace();
            CloseableUtils.closeQuietly(client);
        }
        return client;
    }

}
