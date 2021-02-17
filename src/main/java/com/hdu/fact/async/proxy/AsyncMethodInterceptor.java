package com.hdu.fact.async.proxy;

import com.hdu.fact.async.bean.AsyncMethod;
import com.hdu.fact.async.cache.AsyncProxyCache;
import com.hdu.fact.async.core.AsyncExecutor;
import com.hdu.fact.async.core.AsyncFutureCallable;
import com.hdu.fact.async.core.AsyncFutureTask;
import com.hdu.fact.async.exception.AsyncException;
import com.hdu.fact.async.util.CommonUtil;
import com.hdu.fact.async.util.ReflectionHelper;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;


/**
 * <p>
 * <p>
 * cglib的代理过程，实现MethodInterceptor接口
 *
 * </p>
 */
public class AsyncMethodInterceptor implements MethodInterceptor {

    private Object targetObject;

    public AsyncMethodInterceptor(Object targetObject) {
        this.targetObject = targetObject;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

        final String cacheKey = CommonUtil.buildkey(targetObject, method);

        final AsyncMethod asyncMethod = AsyncProxyCache.getAsyncMethod(cacheKey);

        /** 没获取到或者是finalized方法直接执行吧*/
        if (asyncMethod == null || !ReflectionHelper.canProxyInvoke(method)) {
            return ReflectionHelper.invoke(targetObject, args, method);
        }

        // 如果线程池即将结束，直接调用这个方法结束
        if (AsyncExecutor.isDestroyed()) {
            return ReflectionHelper.invoke(asyncMethod.getObject(), args, method);
        }

        /** */
        final Object[] finArgs = args;

        /**
         *  核心方法，调用AsyncExecutor线程池执行这个方法
         *  记录返回值，将其提供给后续结果代理
         */
        AsyncFutureTask<Object> future = AsyncExecutor.submit(new AsyncFutureCallable<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    /** 执行原来被代理类中的方法*/
                    return ReflectionHelper.invoke(asyncMethod.getObject(), finArgs, asyncMethod.getMethod());
                } catch (Throwable e) {
                    throw new AsyncException(e);
                }
            }

            @Override
            public int maxAttemps() {
                return asyncMethod.getRetry().getMaxAttemps();
            }

            @Override
            public long timeout() {
                return asyncMethod.getTimeout();
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class<? extends Throwable>[] exceptions() {
                return new Class[]{TimeoutException.class};
            }

            @Override
            public String cacheKey() {
                return cacheKey;
            }
        });

        /** 如果是void返回值，直接返回即可*/
        if (asyncMethod.isVoid()) {
            return null;
        }

        /** 在此处构建返回对象的代理，将方法代理并返回*/
        return new AsyncResultProxy(future).buildProxy(method.getReturnType(), asyncMethod.getTimeout(), true);
    }
}
