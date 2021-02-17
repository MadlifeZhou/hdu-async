package com.hdu.fact.async.proxy;


/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */
public interface AsyncProxy {

    public Object buildProxy(Object target, boolean all);

    public Object buildProxy(Object target, long timeout, boolean all);

}
 