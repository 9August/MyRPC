package me.August.MyRPC.exceptons;

/**
 * @Author 9August
 * @Date 2025/6/1 20:11
 * @description:
 */
public class ResponseException extends RuntimeException {

    private byte code;
    private String msg;

    public ResponseException(byte code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
