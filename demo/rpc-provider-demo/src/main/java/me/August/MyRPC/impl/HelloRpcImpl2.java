package me.August.MyRPC.impl;

import me.August.MyRPC.HelloRpc;
import me.August.MyRPC.HelloRpc2;
import me.August.MyRPC.annotation.RpcApi;

@RpcApi
public class HelloRpcImpl2 implements HelloRpc2 {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
