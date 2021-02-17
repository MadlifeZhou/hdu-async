package com.hdu.fact.async.pool;

/**
 * @author zhou
 * @date 2020-12-3 上午10:04:58
 * @description 对Runnable的再次封装
 */


public interface RunnableAround {

    public Runnable advice(Runnable command);

}
 