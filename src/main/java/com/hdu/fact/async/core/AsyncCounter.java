package com.hdu.fact.async.core;


/**
 * 使用一个ThreadLocal来进行包装
 * @author zhou
 */
public class AsyncCounter {

    private static ThreadLocal<Integer> counterThreadMap = new ThreadLocal<>();

    public static int intValue() {
        Integer counter = counterThreadMap.get();
        if (counter == null) {
            counter = 0;
        }
        return counter;
    }

    public static Integer get() {
        return counterThreadMap.get();
    }

    public static void set(Integer counter) {
        if (counter == null) {
            counter = 0;
        }
        counterThreadMap.set(counter);
    }

    public static void release() {
        counterThreadMap.remove();
    }
}
 