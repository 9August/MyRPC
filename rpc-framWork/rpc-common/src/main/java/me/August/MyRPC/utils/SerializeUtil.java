package me.August.MyRPC.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @Author 9August
 * @Date 2025/6/1 23:13
 * @description:
 */
public class SerializeUtil {
    public static byte[] serialize(Object object) {
        // 针对不同的消息类型需要做不同的处理
        // 如果是心跳的请求，没有payload，直接返回null
        if (object == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
//            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }
}
