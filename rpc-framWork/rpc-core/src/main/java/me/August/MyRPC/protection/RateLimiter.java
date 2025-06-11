package me.August.MyRPC.protection;

/**
 * @Author 9August
 * @Date 2025/6/4 16:33
 * @description:
 */
public interface RateLimiter {
    boolean allowRequest();
}
