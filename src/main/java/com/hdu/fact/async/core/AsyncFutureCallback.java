package com.hdu.fact.async.core;

/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */
public interface AsyncFutureCallback<V> {

    /**
     * 执行成功回调方法
     *
     * @param result
     * @author zhou
     * @version
     */
    void onSuccess(V result);

    /**
     * 执行失败回调方法
     *
     * @param t
     * @author woter
     * @version
     */
    void onFailure(Throwable t);

}
