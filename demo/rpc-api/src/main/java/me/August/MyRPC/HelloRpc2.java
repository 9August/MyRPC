package me.August.MyRPC;

import me.August.MyRPC.annotation.TryTimes;

public interface HelloRpc2 {

    @TryTimes(tryTimes = 3, intervalTime = 3000)
    String sayHi(String msg);
}
