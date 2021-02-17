package com.hdu.fact.async.core;

import java.util.Map;

/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */
public abstract class AsyncMap<T> extends AsyncCallable<T> {

    protected Map<String, Object> dataMap;

    void setDateMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

}
 