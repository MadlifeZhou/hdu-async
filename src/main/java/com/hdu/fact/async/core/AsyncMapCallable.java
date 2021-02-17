package com.hdu.fact.async.core;

import java.util.Map;

/**
 * @author zhou
 */
public abstract class AsyncMapCallable<T> extends AsyncMap<T> {

    public AsyncMapCallable() {
    }

    public AsyncMapCallable(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public T doAsync() {
        return doAsync(dataMap);
    }

    public abstract T doAsync(Map<String, Object> dataMap);

}
 