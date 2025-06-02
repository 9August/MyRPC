package me.August.MyRPC.compress;

/**
 * @Author 9August
 * @Date 2025/6/2 10:30
 * @description:
 */
public interface Compressor {
    // 对字节数据进行压缩
    byte[] compress(byte[] bytes);

    // 对字节数据进行解压缩
    byte[] decompress(byte[] bytes);
}
