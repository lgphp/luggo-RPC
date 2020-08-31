- luggo RPC
 
  基于AKKA的一款轻量级RPC框架，具有超低内存消耗，高吞吐，高性能的特征。服务端采用超大规模actor代替线程来处理来自客户端请求
  本框架很容易的与springboot集成。,无需任何配置，即可启动。 发布消费服务非常方便
  
> Feature

- 基于 AKKA的消息通信
- 服务通过zookeeper注册
- 全注解，无xml配置
- 对Springboot友好


> 框架集成

- Provider

   服务提供者，也就是RPC的服务方

    - maven依赖
    
    ```xml
      <dependency>
          <groupId>ai.totok.infra.luggorpc</groupId>
          <artifactId>rpcserver</artifactId>
          <version>0.0.1</version>
      </dependency>
  

    ```
   - application.yml 配置
   
   ```yaml
   
        luggo:
          server:
            addr: 127.0.0.1   #服务方监听地址
            port: 30183   #服务方监听端口
            actor:
              num: 10   #服务方启动actor数量 ，相当于线程池
          register:
            addr: 47.91.111.137:12185  #zookeeper 服务器地址
            group: admin   #服务群组 ，使用group做租户隔离

     ``` 

   - 服务发布
        
        服务的发布只需要在服务的实现类上添加注解`@LugooProvider`  , 同时框架会将此对象自动交给spring容器管辖，也就是说不用使用spring的服务注解
        也可以从spring容器中自动装配
        
        ```java
        @LugooProvider
        public class UserInfoImpl implements IUserInfo {
        
            @Override
            public String getToken(String username) {
                try {
                    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return username;
            }
        }
 
      ``` 
      
      API `IUserInfo 需要实现序列化接口` ，例子如下
      
      ```java
      
      public interface IUserInfo extends Serializable {
      
          String getToken(String username);
      }

      ```
      
    -  ProviderApplication 启动
     
          需要加入ComponentScan
          
             ```java
            @SpringBootApplication
            @ComponentScan("ai.totok.*")
            public class AkkaRpcHttp {}
            ```
    
       
      
   - Consumer
   
      服务消费者，也就是常用的Http端
      
     - maven 依赖
     
        ```xml
               <dependency>
                       <groupId>ai.totok.infra.luggorpc</groupId>
                       <artifactId>rpcclient</artifactId>
                       <version>0.0.1</version>
                   </dependency>
    
        ```
        
     - applicaiton.yml 配置
     
         ```yaml
            luggo:
              client:
                addr: 127.0.0.1 #消费端监听地址
                port: 2012   # 消费端监听端口
              register:
                addr: 47.91.111.137:12185   #zookeeper服务地址
                group: admin   # 服务群组    
         ```
        
          -  ConsumerApplication 启动
           
                需要加入ComponentScan
                
                   ```java
                  @SpringBootApplication
                  @ComponentScan("ai.totok.**")
                  public class AkkaRpcProviderApp {}
                  ```  
        
      - 服务消费
      
      只需要在接口使用注解  ` @LugooConsumer` 
      
      > 注解有三个参数 ， 负责均衡策略 `loadbalance=""`  回退方法 `fallback=""` i 以及请求超时时间 requestTimeOut = `4` (默认5秒)
      
      其中负载均衡目前只能选择默认的随机，后期可以增加 时间hash, 请求ID- hash等
      
      回退的方法必须要和当前service在同一个方法内，不支持带参数。
      
      
      
      
       ```java
          @Service
          public class AkkaService {
              @LugooConsumer(fallback = "testRpcFallback" ,requestTimeOut = 4)
              IUserInfo iUserInfo;
          
              public String  testRpc(int i) {
                  return iUserInfo.getToken(String.valueOf(i));
              }
              private String testRpcFallback(){
                return "fallback";
              }
          }
        ```
      
 
    