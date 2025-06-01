package me.August.MyRPC.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author 9August
 * @Date 2025/6/1 19:33
 * @description: 请求响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse {
    // 请求的id
    private long requestId;

    // 请求的类型，压缩的类型，序列化的方式
    private byte compressType;
    private byte serializeType;

    private long timeStamp;

    // 1 成功，  2 异常
    private byte code;

    // 具体的消息体
    private Object body;
}
