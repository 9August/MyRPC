package me.August.MyRPC.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author 9August
 * @Date 2025/5/31 18:04
 * @description: 服务调用方发起的请求内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRequest {

    // 请求的id
    private long requestId;

    // 请求的类型，压缩的类型，序列化的方式
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    private long timeStamp;

    // 具体的消息体
    private RequestPayload requestPayload;

}