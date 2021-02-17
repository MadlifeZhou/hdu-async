package com.hdu.fact.async.template;

import com.hdu.fact.async.bean.AsyncResult;
import com.hdu.fact.async.constant.AsyncConstant;
import com.hdu.fact.async.core.*;
import com.hdu.fact.async.exception.AsyncException;
import com.hdu.fact.async.proxy.AsyncMethodProxy;
import com.hdu.fact.async.proxy.AsyncProxy;
import com.hdu.fact.async.proxy.AsyncResultProxy;
import com.hdu.fact.async.util.ReflectionHelper;
import com.hdu.fact.async.util.ValidationUtils;
import com.hdu.fact.async.core.*;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * <p>
 * 编程式异步调用模板
 *
 * </p>
 *
 * @author zhou
 */
@SuppressWarnings("all")
public class AsyncTemplate {

    private static AsyncProxy cglibProxy = new AsyncMethodProxy();

    public enum ProxyType {
        CGLIB
    }

    /**
     * <p>
     * <p>
     * 获取代理方式：</br> ProxyType.SPRING 返回Spring Aop代理</br> ProxyType.CGLIB
     * 返回Cglib代理
     *
     * </p>
     *
     * @param type
     * @return
     * @author woter
     * @date 2016-4-14 上午10:42:37
     * @version
     */
    public static AsyncProxy getAsyncProxy(ProxyType type) {
        return cglibProxy;
    }

    /**
     * <p>
     * <p>
     * 构建代理类</br>
     *
     * </p>
     *
     * @param t 需要被代理的类
     * @return T
     * 必须带有返回参数且不支持Void, Array 及 Integer, Long, String, Boolean等Final修饰类</br>
     * 如果需要返回以上类型，可以创建对象包装；如：
     * {@linkplain AsyncResult}
     * @author zhou
     * @version
     */
    public static <T> T buildProxy(T t) {
        return buildProxy(t, 0);
    }

    /**
     * <p>
     * <p>
     * 构建代理类
     *
     * </p>
     *
     * @param t       需要被代理的类
     * @param timeout 超时时间（单位：毫秒）
     * @return T
     * 必须带有返回参数且不支持Void,Array及Integer,Long,String,Boolean等Final修饰类</br>
     * 如果需要返回以上类型，可以创建对象包装；如：
     * {@linkplain AsyncResult}
     * @author zhou
     * @version
     */
    public static <T> T buildProxy(T t, long timeout) {
        return (T) getAsyncProxy(ProxyType.CGLIB).buildProxy(t, timeout, true);
    }

    /**
     * <p>
     * <p>
     * 构建代理类
     *
     * </p>
     *
     * @param T         需要被代理的类
     * @param proxyType 代理类型
     * @return T
     * 必须带有返回参数且不支持Void,Array及Integer,Long,String,Boolean等Final修饰类</br>
     * 如果需要返回以上类型，可以创建对象包装；如：
     * {@linkplain AsyncResult}
     * @author zhou
     * @version
     */
    public static <T> T buildProxy(T t, ProxyType proxyType) {
        return buildProxy(t, AsyncConstant.ASYNC_DEFAULT_TIME_OUT, proxyType);
    }

    /**
     * <p>
     * <p>
     * 构建代理类
     *
     * </p>
     *
     * @param T         需要被代理的类
     * @param timeout   超时时间（单位：毫秒）
     * @param proxyType 代理类型
     * @return T
     * 必须带有返回参数且不支持Void,Array及Integer,Long,String,Boolean等Final修饰类</br>
     * 如果需要返回以上类型，可以创建对象包装；如：
     * {@linkplain AsyncResult}
     * @author zhou
     * @version
     */
    public static <T> T buildProxy(T t, long timeout, ProxyType proxyType) {
        return (T) getAsyncProxy(proxyType).buildProxy(t, timeout, true);
    }

    /**
     * <p>
     * <p>
     *
     * 无返回对象使用execute方法
     * 异步执行AsyncCallable.doAsync
     *
     * </p>
     *
     * @param 实现AsyncCallable接口
     * @param callable
     * @author zhou
     * @version
     */
    public static void execute(AsyncCallable<Void> callable) {
        AsyncExecutor.execute(callable);
    }

    /**
     * <p>
     * <p>
     * 异步执行 AsyncCallable.doAsync方法
     *
     * </p>
     *
     * @param AsyncCallable<T> 需要实现的接口
     * @return T
     * 必须带有返回参数且不支持Void,Array及Integer,Long,String,Boolean等Final修饰类</br>
     * 如果需要返回以上类型，可以创建对象包装；如：
     * {@linkplain AsyncResult}
     * @author zhou
     * @version
     */

    public static <T> T submit(AsyncCallable<T> callable) {
        Type type = callable.getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            // 未指定AsyncCallback的泛型信息
            throw new AsyncException("must be specify AsyncCallable<T> for T type");
        }
        Class<?> returnClass = ReflectionHelper.getGenericClass((ParameterizedType) type, 0);
        return submit(callable, returnClass, callable.timeout());
    }

    /**
     * <p>
     * 异步执行AsyncCallable.doAsync；并且回调AsyncFutureCallback
     * </p>
     *
     * @param asyncCallable
     * @param asyncFutureCallback
     * @author woter
     * @date 2016-8-1 下午5:20:22
     * @version
     */
    public static <T> void submit(AsyncCallable<T> callable, AsyncFutureCallback<T> asyncFutureCallback) {
        AsyncExecutor.submit(callable, asyncFutureCallback);
    }

    /**
     * 遍历List 异步执行AsyncFunction.doAsync；
     * @param list
     * @param function 需要实现的抽象类
     * @return List<E>
     * E 必须带有返回参数且不支持Void,Array及Integer,Long,String,Boolean等Final修饰类</br>
     * 如果需要返回以上类型，可以创建对象包装；如：
     * {@linkplain AsyncResult}
     * @author woter
     * @date 2017-7-17 下午5:50:55
     * @version
     */
    public static <T, E> List<E> submit(List<T> list, final AsyncFunction<T, E> function) {
        List<E> asyncs = new ArrayList<E>();
        if (CollectionUtils.isEmpty(list)) {
            return asyncs;
        }
        if (function == null) {
            return asyncs;
        }

        Class<?> returnClass = ReflectionHelper.getGenericClass(function.getClass(), 1);
        for (final T t : list) {
            asyncs.add(submit(new AsyncCallable<E>() {
                @Override
                public E doAsync() {
                    return function.doAsync(t);
                }
            }, returnClass, function.timeout()));
        }
        return asyncs;
    }

    private static <T> T submit(AsyncCallable<T> callback, Class<?> returnClass, long timeout) {
        ValidationUtils.checkNotNull(callback);
        ValidationUtils.checkNotNull(returnClass, "must be specify return type");

        if (!ReflectionHelper.canProxy(returnClass)) {
            return callback.doAsync();
        }

        if (Void.TYPE.isAssignableFrom(returnClass)) {
            AsyncExecutor.execute(callback);
            return null;
        }

        AsyncFutureTask<T> future = AsyncExecutor.submit(callback);
        return (T) new AsyncResultProxy(future).buildProxy(returnClass, timeout, true);
    }
}
