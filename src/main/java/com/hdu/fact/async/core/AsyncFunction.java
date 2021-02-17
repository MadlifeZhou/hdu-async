package com.hdu.fact.async.core;

import com.hdu.fact.async.constant.AsyncConstant;


/**
 * @author zhou
 */
public abstract class AsyncFunction<T, E> {


    public abstract E doAsync(T t);


    public long timeout() {
        return AsyncConstant.ASYNC_DEFAULT_TIME_OUT;
    }

    public int maxAttemps() {
        return AsyncConstant.ASYNC_DEFAULT_RETRY;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Throwable>[] exceptions() {
        return new Class[]{Throwable.class};
    }

}
 