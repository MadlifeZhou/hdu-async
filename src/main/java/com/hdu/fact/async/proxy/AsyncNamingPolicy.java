package com.hdu.fact.async.proxy;

import org.springframework.cglib.core.DefaultNamingPolicy;

/**
 * 更改命名规则
 * @author zhou
 */
public class AsyncNamingPolicy extends DefaultNamingPolicy {
    public static final AsyncNamingPolicy INSTANCE = new AsyncNamingPolicy();

    @Override
    protected String getTag() {
        return "ByAsyncCGLIB";
    }
}
 