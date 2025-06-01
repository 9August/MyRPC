package me.August.MyRPC.serialize;

import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.config.ObjectWrapper;
import me.August.MyRPC.serialize.impl.HessianSerializer;
import me.August.MyRPC.serialize.impl.JdkSerializer;
import me.August.MyRPC.serialize.impl.JsonSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author 9August
 * @Date 2025/5/31 20:43
 * @description: 序列化
 */
@Slf4j
public class SerializerFactory {
    private final static ConcurrentHashMap<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);
    static {
        ObjectWrapper<Serializer> jdk = new  ObjectWrapper<>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> json = new  ObjectWrapper<>((byte) 2, "json", new JsonSerializer());
        ObjectWrapper<Serializer> hessian = new  ObjectWrapper<>((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);

        SERIALIZER_CACHE_CODE.put((byte) 1, jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2, json);
        SERIALIZER_CACHE_CODE.put((byte) 3, hessian);
    }
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if(serializerWrapper == null){
            log.error("未找到您配置的【{}】序列化工具，默认选用jdk的序列化方式。",serializeType);
            return SERIALIZER_CACHE.get("jdk");
        }

        return SERIALIZER_CACHE.get(serializeType);
    }

    public static  ObjectWrapper<Serializer> getSerializer(Byte serializeCode) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if(serializerWrapper == null){
            log.error("未找到您配置的【{}】序列化工具，默认选用jdk的序列化方式。",serializeCode);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE_CODE.get(serializeCode);
    }

    // 新增一个新的序列化器
    public static void addSerializer(ObjectWrapper<Serializer> serializerObjectWrapper){
        SERIALIZER_CACHE.put(serializerObjectWrapper.getName(),serializerObjectWrapper);
        SERIALIZER_CACHE_CODE.put(serializerObjectWrapper.getCode(),serializerObjectWrapper);
    }
}
