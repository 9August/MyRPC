package me.August.MyRPC.exceptons;

/**
 * @Author 9August
 * @Date 2025/6/2 15:09
 * @description:
 */
public class LoadBalancerException extends RuntimeException {

    public LoadBalancerException(String message) {
        super(message);
    }

    public LoadBalancerException() {
    }
}
