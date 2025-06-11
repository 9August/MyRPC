package me.August.MyRPC.impl;

import me.August.MyRPC.HelloRpc;
import me.August.MyRPC.annotation.RpcApi;

@RpcApi(group = "primary")
public class HelloRpcImpl implements HelloRpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
