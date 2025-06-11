package me.August.MyRPC.channelHandler.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.RpcBootstrap;
import me.August.MyRPC.enumeration.RespCode;
import me.August.MyRPC.exceptons.ResponseException;
import me.August.MyRPC.protection.CircuitBreaker;
import me.August.MyRPC.transport.message.RpcRequest;
import me.August.MyRPC.transport.message.RpcResponse;
import me.August.MyRPC.loadBalancer.LoadBalancer;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @Author 9August
 * @Date 2025/5/31 17:26
 * @description: 客户端最后处理调用结果Handler
 */
@Slf4j
public class SimpleChannelInboundHandler extends io.netty.channel.SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        // 从全局的挂起的请求中寻找与之匹配的待处理的completableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(rpcResponse.getRequestId());

        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = RpcBootstrap.getInstance()
                .getConfiguration().getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);

        byte code = rpcResponse.getCode();

        if(code == RespCode.FAIL.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，返回错误的结果，响应码[{}].",
                    rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,RespCode.FAIL.getDesc());

        } else if (code == RespCode.RATE_LIMIT.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，被限流，响应码[{}].",
                    rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,RespCode.RATE_LIMIT.getDesc());

        } else if (code == RespCode.RESOURCE_NOT_FOUND.getCode() ){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，未找到目标资源，响应码[{}].",
                    rpcResponse.getRequestId(),rpcResponse.getCode());
            throw new ResponseException(code,RespCode.RESOURCE_NOT_FOUND.getDesc());

        } else if (code == RespCode.SUCCESS.getCode() ){
            // 服务提供方，给予的结果
            Object returnValue = rpcResponse.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture，处理响应结果。", rpcResponse.getRequestId());
            }
        } else if(code == RespCode.SUCCESS_HEART_BEAT.getCode()){
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture,处理心跳检测，处理响应结果。", rpcResponse.getRequestId());
            }
        } else if(code == RespCode.BECOLSING.getCode()){
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("当前id为[{}]的请求，访问被拒绝，目标服务器正处于关闭中，响应码[{}].",
                        rpcResponse.getRequestId(),rpcResponse.getCode());
            }

            // 修正负载均衡器
            // 从健康列表中移除
            RpcBootstrap.CHANNEL_CACHE.remove(socketAddress);
            // reLoadBalance
            LoadBalancer loadBalancer = RpcBootstrap.getInstance()
                    .getConfiguration().getLoadBalancer();
            // 重新进行负载均衡
            RpcRequest rpcRequest = RpcBootstrap.REQUEST_THREAD_LOCAL.get();
            loadBalancer.reLoadBalance(rpcRequest.getRequestPayload().getInterfaceName()
                    ,RpcBootstrap.CHANNEL_CACHE.keySet().stream().toList());

            throw new ResponseException(code,RespCode.BECOLSING.getDesc());
        }

    }
}
