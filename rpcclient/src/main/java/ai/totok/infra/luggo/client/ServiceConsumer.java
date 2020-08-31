package ai.totok.infra.luggo.client;

import ai.totok.infra.luggo.annotation.LugooConsumer;
import ai.totok.infra.luggo.core.loadbalance.ILoadBalance;
import akka.actor.ActorRef;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lgphp
 * @date 2020-03-19 15:10
 * @description
 */

@Component
@Slf4j
public class ServiceConsumer {

    private CuratorFramework registerClient;
    private ApplicationContext ctx;

    public ServiceConsumer(ApplicationContext applicationContext, @Qualifier("zkClient") CuratorFramework registerClient) {

        this.registerClient = registerClient;
        this.ctx = applicationContext;
        this.getReferencefromZookeeper();
    }

    /**
     * 根据业务信息创建动态代理
     *
     * @param serviceInfo
     */
    public void doConsumer(JSONObject serviceInfo) {
        if (Objects.nonNull(serviceInfo)) {
            String srvAddr = serviceInfo.getString("srvaddr");
            int port = serviceInfo.getIntValue("port");
            int actorNum = serviceInfo.getIntValue("actnum");
            String serviceInterface = serviceInfo.getString("srvname");
            String[] beanDefinitionNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanDefinitionNames);
            for (String clsName : beanDefinitionNames) {
                // 防止循环引用，需要把自身排除在外
                if (!clsName.equalsIgnoreCase(this.getClass().getSimpleName())) {
                    Class<?> aClass = ctx.getBean(clsName).getClass();
                    // 获取每个类中有注解的字段
                    Field[] declaredFields = aClass.getDeclaredFields();
                    for (Field f : declaredFields) {
                        if (f.isAnnotationPresent(LugooConsumer.class)) {
                            Class<?> interFaceCls = f.getType();
                            if (interFaceCls.getCanonicalName().equals(serviceInterface)) {
                                f.setAccessible(true);
                                try {
                                    LugooConsumer annotation = f.getAnnotation(LugooConsumer.class);
                                    // 根据注解获取均衡的实现方式
                                    String loadbalanceType = annotation.loadbalance();
                                    // 获取回退的方法名
                                    String fallbackMethod = annotation.fallback();
                                    int requestTimeout = annotation.requestTimeOut();
                                    Class<?> lbCls = Class.forName("ai.totok.infra.luggo.core.loadbalance." + loadbalanceType);
                                    ILoadBalance loadBalance = (ILoadBalance) lbCls.newInstance();
                                    // 创建路由Channel
                                    synchronized (interFaceCls) {
                                        RouterChannel.createRouter(srvAddr, port, actorNum, interFaceCls.getCanonicalName());
                                    }
                                    Object service = RemoteProxyFactory.createService(loadBalance, interFaceCls, ctx.getBean(clsName), fallbackMethod, requestTimeout);
                                    f.set(ctx.getBean(clsName), service);
                                } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * 从zookeeper中获取服务信息
     */
    public void getReferencefromZookeeper() {
        PathChildrenCache cache = new PathChildrenCache(registerClient, "/service", true);
        try {
            cache.start(PathChildrenCache.StartMode.NORMAL);
            cache.getListenable().addListener((client, event) -> {
                String path;
                byte[] data;
                JSONObject serviceInfo;
                switch (event.getType()) {
                    case CHILD_ADDED: //  服务上线
                        path = event.getData().getPath();
                        data = client.getData().forPath(path);
                        serviceInfo = JSONObject.parseObject(new String(data));
                        doConsumer(serviceInfo);
                        break;
                    case CHILD_REMOVED:
                        // 如果有服务下线，则删除路由
                        path = event.getData().getPath();
                        removeServiceOfRouter(path);
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            log.error("can not connect zookeeper:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 移除服务
     *
     * @param path
     */
    private void removeServiceOfRouter(String path) {
        String removeName = path.split(":")[2];
        String removePath = path.split("/")[2];
        // 获取这个业务下的所有路由
        List<Map<String, ActorRef>> routerMaps = RouterChannel.routerChannel.get(removeName);
        List<Map<String, ActorRef>> newListRoute = Lists.<Map<String, ActorRef>>newArrayList();
        // 移除该业务的router
        synchronized (this) {
            for (Map m : routerMaps) {
                if (Objects.isNull(m.get(removePath))) {
                    newListRoute.add(m);
                }
            }
            RouterChannel.routerChannel.replaceValues(removeName, newListRoute);
        }

    }
}
