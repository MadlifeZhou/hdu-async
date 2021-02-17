package com.hdu.fact.async.processor;

/**
 * @author zhou
 */
public class RetryResult<T> {

    /** 设置一个结果和一个异常*/
    private T data;
    private Throwable throwable;


    public RetryResult() {
    }

    public RetryResult(T data, Throwable throwable) {
        this.data = data;
        this.throwable = throwable;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public RetryResult<T> setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

}
 