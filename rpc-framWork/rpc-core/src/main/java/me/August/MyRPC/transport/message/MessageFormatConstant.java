package me.August.MyRPC.transport.message;

/**
 * @Author 9August
 * @Date 2025/5/31 21:18
 * @description: 报文封装常量
 */
public class MessageFormatConstant {
    // 魔数，协议标识
    public final static byte[] MAGIC = "augRpc".getBytes();
    // 版本号
    public final static byte VERSION = 1;

    // 头部信息的总长度
    public final static short HEADER_LENGTH = (byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1  + 8+ 8);
    // 头部信息长度占用的字节数
    public static final int HEADER_FIELD_LENGTH = 2;

    public final static int MAX_FRAME_LENGTH = 1024 * 1024;

    public static final int VERSION_LENGTH = 1;

    // 总长度占用的字节数
    public static final int FULL_FIELD_LENGTH = 4;


}
