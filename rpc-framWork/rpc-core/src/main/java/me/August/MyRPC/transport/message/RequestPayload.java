package me.August.MyRPC.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author 9August
 * @Date 2025/5/31 18:08
 * @description: 具体的消息体(调用方所请求的接口方法的描述)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {
    // 1、接口的名字
    private String interfaceName;

    // 2、方法的名字
    private String methodName;

    // 3、参数列表，参数分为参数类型和具体的参数
    // 参数类型用来确定重载方法，具体的参数用来执行方法调用
    private Class<?>[] parametersType;
    private Object[] parametersValue;

    // 4、返回值的封装
    private Class<?> returnType;
}
