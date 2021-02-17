package com.hdu.fact.async.annotation;

import com.hdu.fact.async.constant.AsyncConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */
@Target({TYPE, FIELD, METHOD})
@Retention(RUNTIME)
public @interface Async {

    /**
     * <p>
     * <p>
     * 调用超时设置-单位毫秒(默认0-不超时)
     *
     * </p>
     *
     * @return
     * @author zhou
     * @version
     */
    long timeout() default AsyncConstant.ASYNC_DEFAULT_TIME_OUT;


    /**
     * <p>
     * <p>
     * 异步调用异常了最多重试次数
     *
     * </p>
     *
     * @return
     * @author zhou
     * @version
     */
    int maxAttemps() default AsyncConstant.ASYNC_DEFAULT_RETRY;


    /***
     *
     * @return
     *
     * @author zhou
     * @version
     */
    Class<? extends Throwable>[] exceptions() default Throwable.class;

}
 