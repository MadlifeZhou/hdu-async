package com.hdu.fact.async.bean;

import java.io.Serializable;

/**
 * <p>
 * <p>
 * 异步执行返回结果包装类</br>
 * 主要用于 void, array 及 Integer, Long, String, Boolean 等 Final修饰类
 *
 * </p>
 *
 * @author zhou
 */
public class AsyncResult<T> implements Serializable {

    /**
     * <p>
     *
     *
     *
     * </p>
     *
     * @author zhou
     * @version
     */
    private static final long serialVersionUID = -3289683114806441520L;

    private T data;

    public AsyncResult() {
    }

    public AsyncResult(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public AsyncResult<T> setData(T data) {
        this.data = data;
        return this;
    }

}
