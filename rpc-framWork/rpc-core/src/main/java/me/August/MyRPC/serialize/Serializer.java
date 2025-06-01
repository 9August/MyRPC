package me.August.MyRPC.serialize;

/**
 * @Author 9August
 * @Date 2025/6/1 23:20
 * @description:
 */
public interface Serializer {

    // 序列化
    byte[] serialize(Object object);

    // 反序列化
    <T> T deserialize(byte[] bytes, Class<T> clazz);

}
