package me.August.MyRPC.spi;

import lombok.extern.slf4j.Slf4j;
import me.August.MyRPC.config.ObjectWrapper;
import me.August.MyRPC.exceptons.SpiException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author 9August
 * @Date 2025/6/3 20:23
 * @description: 实现一个简易版本的spi
 */
@Slf4j
public class SpiHandler {
    // 定义一个basePath
    private static final String BASE_PATH = "META-INF/rpc-services";
    // 缓存spi相关内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);
    // 缓存的是每一个接口所对应的实现的实例
    private static final Map<Class<?>, List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);


    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        if (fileUrl != null) {
            File file = new File(fileUrl.getPath());
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key, value);
                }
            }
        }

    }

    private static List<String> getImplNames(File child) {
        try (
                FileReader fileReader = new FileReader(child);
                BufferedReader bufferedReader = new BufferedReader(fileReader)
        ) {
            List<String> implNames = new ArrayList<>();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)) break;
                implNames.add(line);
            }
            return implNames;
        } catch (IOException e) {
            log.error("读取spi文件时发生异常.", e);
        }
        return null;
    }


    // 获取所有服务相关的接口实例
    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz) {
        // 1、优先走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return objectWrappers.stream().map( wrapper -> (ObjectWrapper<T>)wrapper )
                    .collect(Collectors.toList());
        }

        // 2、构建缓存
        buildCache(clazz);

        // 3、再次获取
        objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return objectWrappers.stream().map( wrapper -> (ObjectWrapper<T>)wrapper )
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    private static void buildCache(Class<?> clazz) {
        // 1、通过clazz获取与之匹配的实现名称
        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);
        if (implNames == null || implNames.size() == 0) {
            return;
        }
        // 2、实例化所有的实现
        List<ObjectWrapper<?>> impls = new ArrayList<>();
        for (String implName : implNames) {
            try {
                // 首先进行分割
                String[] codeAndTypeAndName = implName.split("-");
                System.out.println("codeAndTypeAndName = " + codeAndTypeAndName);
                System.out.println("codeAndTypeAndName.length = " + codeAndTypeAndName.length);
                if (codeAndTypeAndName.length != 3) {
                    throw new SpiException("您配置的spi文件不合法");
                }
                Byte code = Byte.valueOf(codeAndTypeAndName[0]);
                String type = codeAndTypeAndName[1];
                String implementName = codeAndTypeAndName[2];

                Class<?> aClass = Class.forName(implementName);
                Object impl = aClass.getConstructor().newInstance();
                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code, type, impl);
                impls.add(objectWrapper);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                log.error("实例化【{}】的实现时发生了异常", implName, e);
            }
        }
        SPI_IMPLEMENT.put(clazz, impls);
    }


}
