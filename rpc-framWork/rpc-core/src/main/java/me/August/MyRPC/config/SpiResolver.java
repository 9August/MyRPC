package me.August.MyRPC.config;
import me.August.MyRPC.compress.Compressor;
import me.August.MyRPC.compress.CompressorFactory;
import me.August.MyRPC.loadBalancer.LoadBalancer;
import me.August.MyRPC.serialize.Serializer;
import me.August.MyRPC.serialize.SerializerFactory;
import me.August.MyRPC.spi.SpiHandler;

import java.util.List;

/**
 * @Author 9August
 * @Date 2025/6/3 20:04
 * @description: spi机制解析配置
 */
public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        // 我的spi的文件中配置了很多实现（自由定义，只能配置一个实现，还是多个）
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        // 将其放入工厂
        if(loadBalancerWrappers != null && loadBalancerWrappers.size() > 0){
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> objectWrappers = SpiHandler.getList(Compressor.class);
        if(objectWrappers != null){
            objectWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerObjectWrappers = SpiHandler.getList(Serializer.class);
        if (serializerObjectWrappers != null){
            serializerObjectWrappers.forEach(SerializerFactory::addSerializer);
        }

    }
}
