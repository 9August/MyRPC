package me.August.MyRPC.channelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.compress.Compressor;
import me.August.MyRPC.compress.CompressorFactory;
import me.August.MyRPC.serialize.Serializer;
import me.August.MyRPC.serialize.SerializerFactory;
import me.August.MyRPC.transport.message.MessageFormatConstant;
import me.August.MyRPC.transport.message.RpcResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @Author 9August
 * @Date 2025/6/1 19:41
 * @description:
 */
@Slf4j
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        // 写入请求头
        // 6个字节的魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2个字节的头部的长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 总长度不清楚，先占位置，后续填值
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 3个类型
        byteBuf.writeByte(rpcResponse.getCode());

        byteBuf.writeByte(rpcResponse.getSerializeType());
        byteBuf.writeByte(rpcResponse.getCompressType());
        // 8字节的请求id
        byteBuf.writeLong(rpcResponse.getRequestId());
        // 8字节的时间戳
        byteBuf.writeLong(rpcResponse.getTimeStamp());


        // 1、对响应做序列化
        byte[] body = null;
        if (rpcResponse.getBody() != null) {
            Serializer serializer = SerializerFactory.getSerializer(rpcResponse.getSerializeType()).getImpl();
            body = serializer.serialize(rpcResponse.getBody());

            // 2、压缩
            Compressor compressor = CompressorFactory.getCompressor(rpcResponse.getCompressType()).getImpl();
            body = compressor.compress(body);
        }

        // 写入请求体（requestPayload）
        if (body != null) {
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;

        // 重新处理报文的总长度
        // 先保存当前的写指针的位置
        int writerIndex = byteBuf.writerIndex();
        // 将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH
        );
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        // 将写指针归位
        byteBuf.writerIndex(writerIndex);
        if (log.isDebugEnabled()) {
            log.debug("响应【{}】已经在服务端完成编码工作。", rpcResponse.getRequestId());
        }

    }


}
