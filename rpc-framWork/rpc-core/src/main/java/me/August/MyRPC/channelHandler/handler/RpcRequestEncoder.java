package me.August.MyRPC.channelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.enumeration.RequestType;
import me.August.MyRPC.transport.message.MessageFormatConstant;
import me.August.MyRPC.transport.message.RequestPayload;
import me.August.MyRPC.transport.message.RpcRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @Author 9August
 * @Date 2025/5/31 20:52
 * @description: 出站时，第二个经过的处理器，对报文进行封装，如序列化，压缩。
 * 报文封装协议如下所示
 */
@Slf4j
public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {

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
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializeType());
        byteBuf.writeByte(rpcRequest.getCompressType());
        // 8字节的请求id
        byteBuf.writeLong(rpcRequest.getRequestId());
        // 8字节的时间戳
        byteBuf.writeLong(rpcRequest.getTimeStamp());

        // 如果是心跳请求，就不处理请求体
        if(rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()){
            // 处理一下总长度，其实总长度 = header长度
            int writerIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH
            );
            byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH);
            byteBuf.writerIndex(writerIndex);
            return;
        }

        // 写入请求体（requestPayload）

        byte[] body = getBodys(rpcRequest.getRequestPayload());
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
            log.debug("请求【{}】已经完成报文的编码。", rpcRequest.getRequestId());
        }

    }

    private byte[] getBodys(RequestPayload requestPayload) {
        if(requestPayload==null){
            return null;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(requestPayload);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
