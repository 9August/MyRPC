package me.August.MyRPC.exceptons;

/**
 * @Author 9August
 * @Date 2025/6/2 13:32
 * @description:
 */
public class CompressException extends RuntimeException{
    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
