package me.August.MyRPC.exceptons;

public class NetworkException extends RuntimeException{
    
    public NetworkException() {
    }
    
    public NetworkException(String message) {
        super(message);
    }
    
    public NetworkException(Throwable cause) {
        super(cause);
    }
}
