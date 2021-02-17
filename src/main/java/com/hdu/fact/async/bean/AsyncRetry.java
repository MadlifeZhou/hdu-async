package com.hdu.fact.async.bean;

/**
 * @author zhou
 */
public class AsyncRetry {

    /** 重试次数*/
    private int maxAttemps;

    /** 抛出的异常*/
    private Class<?>[] exceptions;

    public AsyncRetry() {
    }

    public AsyncRetry(int maxAttemps, Class<?>... exceptions) {
        if (maxAttemps < 0) {
            maxAttemps = 0;
        }
        this.maxAttemps = maxAttemps;
        this.exceptions = exceptions;
    }

    /**
     * 是否可以重试
     * @return
     */
    public boolean canRetry() {
        return maxAttemps > 0;
    }

    public int getMaxAttemps() {
        return maxAttemps;
    }

    public void setMaxAttemps(int maxAttemps) {
        if (maxAttemps < 0) {
            maxAttemps = 0;
        }
        this.maxAttemps = maxAttemps;
    }

    public Class<?>[] getExceptions() {
        return exceptions;
    }

    public void setExceptions(Class<?>[] exceptions) {
        this.exceptions = exceptions;
    }



}
