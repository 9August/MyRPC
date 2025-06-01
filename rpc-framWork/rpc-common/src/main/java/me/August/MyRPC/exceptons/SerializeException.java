package me.August.MyRPC.exceptons;

/**
 * @Author 9August
 * @Date 2025/6/1 23:23
 * @description:
 */
public class SerializeException extends RuntimeException{
    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }
}
