package me.August.MyRPC.compress;

import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.compress.impl.GzipCompressor;
import me.August.MyRPC.config.ObjectWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author 9August
 * @Date 2025/6/2 10:31
 * @description: 压缩工厂
 */
@Slf4j
public class CompressorFactory {
    private final static Map<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);
    }


    // 序列化的类型
    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorObjectWrapper == null) {
            log.error("未找到您配置的【{}】压缩算法，默认选用gzip算法。", compressorType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }

    public static ObjectWrapper<Compressor> getCompressor(byte compressorCode) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        if ((compressorObjectWrapper == null)) {
            log.error("未找到您配置的编号为【{}】的压缩算法，默认选用gzip算法。", compressorCode);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }


    // 给工厂中新增一个压缩方式
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper) {
        COMPRESSOR_CACHE.put(compressorObjectWrapper.getName(), compressorObjectWrapper);
        COMPRESSOR_CACHE_CODE.put(compressorObjectWrapper.getCode(), compressorObjectWrapper);
    }

}
