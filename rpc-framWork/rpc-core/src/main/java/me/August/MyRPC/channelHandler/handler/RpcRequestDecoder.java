package me.August.MyRPC.channelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.compress.Compressor;
import me.August.MyRPC.compress.CompressorFactory;
import me.August.MyRPC.enumeration.RequestType;
import me.August.MyRPC.serialize.Serializer;
import me.August.MyRPC.serialize.SerializerFactory;
import me.August.MyRPC.transport.message.MessageFormatConstant;
import me.August.MyRPC.transport.message.RequestPayload;
import me.August.MyRPC.transport.message.RpcRequest;

import java.io.*;
import java.util.Random;

/**
 * @Author 9August
 * @Date 2025/5/31 22:18
 * @description: 基于长度的逐帧解码
 */
@Slf4j
public class RpcRequestDecoder extends LengthFieldBasedFrameDecoder {

    public RpcRequestDecoder() {
        super(
                // 找到当前报文的总长度，截取报文，截取出来的报文我们可以去进行解析
                // 最大帧的长度，超过这个maxFrameLength值会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度的字段的偏移量，
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // 长度的字段的长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                // todo 负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
                0
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 测试最短响应时间负载均衡器
        Thread.sleep(new Random().nextInt(50));

        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 1、解析魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        // 检测魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("The request obtained is not legitimate。");
            }
        }

        // 2、解析版本号
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("获得的请求版本不被支持。");
        }

        // 3、解析头部的长度
        short headLength = byteBuf.readShort();

        // 4、解析总长度
        int fullLength = byteBuf.readInt();

        // 5、请求类型
        byte requestType = byteBuf.readByte();

        // 6、序列化类型
        byte serializeType = byteBuf.readByte();

        // 7、压缩类型
        byte compressType = byteBuf.readByte();

        // 8、请求id
        long requestId = byteBuf.readLong();

        // 9、时间戳
        long timeStamp = byteBuf.readLong();

        //封装
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(requestType);
        rpcRequest.setCompressType(compressType);
        rpcRequest.setSerializeType(serializeType);
        rpcRequest.setRequestId(requestId);
        rpcRequest.setTimeStamp(timeStamp);

        // 心跳请求没有负载，此处可以判断并直接返回
        if( requestType == RequestType.HEART_BEAT.getId()){
            return rpcRequest;
        }

        int payloadLength = fullLength - headLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        if (payload != null && payload.length != 0) {
            // 1、解压缩
            Compressor compressor = CompressorFactory.getCompressor(compressType).getImpl();
            payload = compressor.decompress(payload);
            // 2、反序列化
            Serializer serializer = SerializerFactory.getSerializer(serializeType).getImpl();
            RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
            rpcRequest.setRequestPayload(requestPayload);
        }

        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已经在服务端完成解码工作。", rpcRequest.getRequestId());
        }

        return rpcRequest;
    }


}
