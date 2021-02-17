package com.hdu.fact.async.bean;

import com.hdu.fact.async.util.ReflectionHelper;

import java.lang.reflect.Method;

/**
 * <p>
 *
 * 对Method的再次封装，
 * 是否为void
 * retry次数
 * 属于哪个对象
 * 重试时间
 *
 * </p>
 *
 * @author zhou
 */

public class AsyncMethod {

    private long timeout;
    private boolean isVoid;
    private Object object;
    private Method method;
    private AsyncRetry retry;

    /**
     * @param object
     * @param method
     * @param timeout
     */
    public AsyncMethod(Object object, Method method, long timeout) {
        this.object = object;
        this.method = method;
        this.timeout = timeout;
        this.isVoid = ReflectionHelper.isVoid(method);
    }

    public AsyncMethod(Object object, Method method, long timeout, AsyncRetry retries) {
        this.object = object;
        this.method = method;
        this.timeout = timeout;
        this.retry = retries;
        this.isVoid = ReflectionHelper.isVoid(method);
    }

    public Method getMethod() {
        return method;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public void setMethod(Method method) {
        this.method = method;
        this.isVoid = ReflectionHelper.isVoid(method);
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }


    public Object getObject() {
        return object;
    }


    public void setObject(Object object) {
        this.object = object;
    }


    public AsyncRetry getRetry() {
        return retry;
    }


    public void setRetry(AsyncRetry retry) {
        this.retry = retry;
    }

    public void setVoid(boolean isVoid) {
        this.isVoid = isVoid;
    }
}
 