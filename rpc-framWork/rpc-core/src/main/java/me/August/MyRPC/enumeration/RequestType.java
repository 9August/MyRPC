package me.August.MyRPC.enumeration;

/**
 * @Author 9August
 * @Date 2025/5/31 20:40
 * @description: 用来标记请求类型
 */
public enum RequestType {
    REQUEST((byte)1,"普通请求"), HEART_BEAT((byte)2,"心跳检测请求");

    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
