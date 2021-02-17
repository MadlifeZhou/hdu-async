package com.hdu.fact.async.proxy;

import com.hdu.fact.async.cache.AsyncProxyCache;
import com.hdu.fact.async.constant.AsyncConstant;
import com.hdu.fact.async.core.AsyncFutureTask;
import com.hdu.fact.async.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;


@SuppressWarnings("all")
public class AsyncResultProxy implements AsyncProxy {

    private final static Logger logger = LoggerFactory.getLogger(AsyncResultProxy.class);

    private AsyncFutureTask future;

    public AsyncResultProxy(AsyncFutureTask future) {
        this.future = future;
    }

    @Override
    public Object buildProxy(Object t, boolean all) {
        return buildProxy(t, AsyncConstant.ASYNC_DEFAULT_TIME_OUT, true);
    }

    /**
     * 调用方法
     * new AsyncResultProxy(future).buildProxy(method.getReturnType(), asyncMethod.getTimeout(), true);
     *
     * @param returnType  method.getReturnType() 之前调用方法的返回类型
     * @param timeout  asyncMethod.getTimeout()
     * @param all
     * @return
     */

    @Override
    public Object buildProxy(Object returnType, long timeout, boolean all) {
        Class<?> returnClass = returnType.getClass();
        if (returnType instanceof Class) {
            returnClass = (Class) returnType;
        }
        /** 先从缓存中获取*/
        Class<?> proxyClass = AsyncProxyCache.getProxyClass(returnClass.getName());
        if (proxyClass == null) {
            Enhancer enhancer = new Enhancer();
            if (returnClass.isInterface()) {
                enhancer.setInterfaces(new Class[]{returnClass});
            } else {
                enhancer.setSuperclass(returnClass);
            }
            enhancer.setNamingPolicy(AsyncNamingPolicy.INSTANCE);
            enhancer.setCallbackType(AsyncResultInterceptor.class);

            /** 创建结果代理类文件*/
            proxyClass = enhancer.createClass();
            System.out.println("======" + proxyClass.getName());
            logger.debug("create result proxy class:{}", returnClass);

            /**
             * 将该类缓存至cache
             */
            AsyncProxyCache.registerProxy(returnClass.getName(), proxyClass);
        }

        /** 将future传入，获取值
         * 使用AsyncResultInterceptor来获取结果
         * */
        Enhancer.registerCallbacks(proxyClass, new Callback[]{new AsyncResultInterceptor(future, timeout)});

        Object proxyObject = null;
        try {
            proxyObject = ReflectionHelper.newInstance(proxyClass);
        } finally {
            /** 静态方法不设置callback*/
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }
        return proxyObject;
    }
}
