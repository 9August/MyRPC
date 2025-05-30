package me.August.MyRPC.exceptons;

public class ZookeeperException extends RuntimeException{
    
    public ZookeeperException() {
    }
    
    public ZookeeperException(Throwable cause) {
        super(cause);
    }
}