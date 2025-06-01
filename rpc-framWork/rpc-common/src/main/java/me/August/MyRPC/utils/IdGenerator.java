package me.August.MyRPC.utils;

import java.util.concurrent.atomic.LongAdder;

/**
 * @Author 9August
 * @Date 2025/6/1 21:03
 * @description: 按雪花算法实现请求id的生成
 * 机房号 5bit
 * 机器号 5bit
 * 时间戳 42bit
 * 序列号 12bit 同一时间下生成的id
 * 5+5+42+12
 */
public class IdGenerator {
    // 起始时间戳
    public static final long START_STAMP = DateUtil.get("2025-5-1").getTime();
    //机房号
    public static final long DATA_CENTER_BIT = 5L;
    // 机器号
    public static final long MACHINE_BIT = 5L;
    // 序列号
    public static final long SEQUENCE_BIT = 12L;

    // 最大值 Math.pow(2,5) -1
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);


    // 101010101010101010101010101010101010101011 10101 10101 101011010101
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();

    // 处理时间戳，时钟回拨
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        // 判断传世的参数是否合法
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("你传入的数据中心编号或机器号不合法.");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        // 第一步：处理时间戳的问题
        long currentTime = System.currentTimeMillis();

        long timeStamp = currentTime - START_STAMP;

        // 判断时钟回拨
        if (timeStamp < lastTimeStamp) {
            throw new RuntimeException("您的服务器进行了时钟回调.");
        }

        // sequenceId需要做一些处理，如果是同一个时间节点，必须自增
        if (timeStamp == lastTimeStamp) {
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX) {
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }

        // 执行结束将时间戳赋值给lastTimeStamp
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT | sequence;

    }

    private long getNextTimeStamp() {
        // 获取当前的时间戳
        long current = System.currentTimeMillis() - START_STAMP;
        // 如果一样就一直循环，直到下一个时间戳
        while (current == lastTimeStamp) {
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1, 2);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> System.out.println(idGenerator.getId())).start();
        }

    }

}
