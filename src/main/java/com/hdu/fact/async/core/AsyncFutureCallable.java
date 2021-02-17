package com.hdu.fact.async.core;

import java.util.concurrent.Callable;


/**
 * @author zhou
 */
public interface AsyncFutureCallable<V> extends Callable<V> {

    long timeout();

    int maxAttemps();

    Class<? extends Throwable>[] exceptions();

    String cacheKey();

}
 