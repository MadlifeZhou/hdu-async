package com.hdu.fact.async.core;

import java.util.Map;


/**
 *
 * @author zhou
 */
public abstract class AsyncMapRunnable extends AsyncMap<Void> {

    public AsyncMapRunnable() {
    }

    public AsyncMapRunnable(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public Void doAsync() {
        doAsync(dataMap);
        return null;
    }

    public abstract void doAsync(Map<String, Object> dataMap);

}
 