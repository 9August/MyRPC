package me.August.MyRPC;

import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.discovery.RegistryConfig;


@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) {
        // 使用ReferenceConfig进行封装一个代理对象
        ReferenceConfig<HelloRpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloRpc.class);

        // 代理做了些什么?
        // 1、连接注册中心
        // 2、拉取服务列表
        // 3、选择一个服务并建立连接
        // 4、发送请求，携带一些信息（接口名，参数列表，方法的名字），获得结果

        RpcBootstrap.getInstance()
                .application("first-yrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .compress("gzip")
                .group("primary")
                .reference(reference);

//        System.out.println("++------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        HelloRpc hellorpc = reference.get();

        while (true) {
            try {
                Thread.sleep(30000);
                System.out.println("++------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

//        String sayHi = hellorpc.sayHi("你好rpc");

        for (int i = 0; i < 10; i++) {
            String sayHi = hellorpc.sayHi("你好rpc");
            log.info("sayHi-->{}", sayHi);
        }


//        log.info("sayHi-->{}", sayHi);
//            for (int i = 0; i < 10; i++) {
//                String sayHi = hellorpc.sayHi("你好rpc");
//                log.info("sayHi-->{}", sayHi);
//            }
        }

    }
}
