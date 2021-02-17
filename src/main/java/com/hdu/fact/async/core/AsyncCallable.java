package com.hdu.fact.async.core;

import com.hdu.fact.async.constant.AsyncConstant;


/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */

public abstract class AsyncCallable<T> implements AsyncFutureCallable<T> {

    @Override
    public T call() {
        return doAsync();
    }

    public abstract T doAsync();

    /**
     * 调用超时设置-单位毫秒(默认0-不超时)
     */
    @Override
    public long timeout() {
        return AsyncConstant.ASYNC_DEFAULT_TIME_OUT;
    }

    /**
     * 最多重试次数
     */
    @Override
    public int maxAttemps() {
        return AsyncConstant.ASYNC_DEFAULT_RETRY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Throwable>[] exceptions() {
        return new Class[]{Throwable.class};
    }

    @Override
    public final String cacheKey() {
        return null;
    }

}
 