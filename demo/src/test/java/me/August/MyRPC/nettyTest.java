package me.August.MyRPC;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class nettyTest {

    @Test
    public void fun() {
        ByteBuf header = Unpooled.buffer();// 模拟http请求头
        ByteBuf body = Unpooled.buffer();// 模拟http请求主体
        CompositeByteBuf httpBuf = Unpooled.compositeBuffer();
// 这一步，不需要进行header和body的额外复制，httpBuf只是持有了header和body的引用
// 接下来就可以正常操作完整httpBuf了
        httpBuf.addComponents(header, body);
    }

}
