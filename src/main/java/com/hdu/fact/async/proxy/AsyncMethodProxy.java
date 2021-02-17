package com.hdu.fact.async.proxy;

import com.hdu.fact.async.cache.AsyncProxyCache;
import com.hdu.fact.async.constant.AsyncConstant;
import com.hdu.fact.async.exception.AsyncException;
import com.hdu.fact.async.util.CommonUtil;
import com.hdu.fact.async.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;

/**
 * <p>
 * 建立cglib proxy代理类
 * </p>
 *
 * @author zhou
 */
@SuppressWarnings("all")
public class AsyncMethodProxy implements AsyncProxy {

    private final static Logger logger = LoggerFactory.getLogger(AsyncMethodProxy.class);

    @Override
    public Object buildProxy(Object target, boolean all) {
        return buildProxy(target, AsyncConstant.ASYNC_DEFAULT_TIME_OUT, all);
    }

    /**
     * 使用cglib完成代理
     * 先从缓存中读取
     * @param target 被代理对象
     * @param timeout
     * @param all 是否代理全部方法
     * @return
     */
    @Override
    public Object buildProxy(Object target, long timeout, boolean all) {
        Class<?> targetClass = CommonUtil.getClass(target);

        if (target instanceof Class) {
            throw new AsyncException("target is not object instance");
        }

        // 如果是基本数据类型，无法代理，直接将这个类返回
        if (!ReflectionHelper.canProxy(targetClass)) {
            return target;
        }

        // 先从缓存中去取出代理方法，如果没有再建立代理
        Class<?> proxyClass = AsyncProxyCache.getProxyClass(CommonUtil.buildkey(targetClass.getName(), all));

        // 如果该类没有被代理，使用cglib构建代理
        if (proxyClass == null) {
            Enhancer enhancer = new Enhancer();
            if (targetClass.isInterface()) {
                enhancer.setInterfaces(new Class[]{targetClass});
            } else {
                enhancer.setSuperclass(targetClass);
            }
            enhancer.setNamingPolicy(AsyncNamingPolicy.INSTANCE);
            /** 设置拦截方法类型*/
            enhancer.setCallbackType(AsyncMethodInterceptor.class);

            /** 创建代理类*/
            proxyClass = enhancer.createClass();
            logger.debug("create proxy class:{}", targetClass);

            /** 将ProxyClass代理对象和方法加入*/
            AsyncProxyCache.registerProxy(CommonUtil.buildkey(targetClass.getName(), all), proxyClass);
            AsyncProxyCache.registerMethod(target, timeout, all);
        }
        /** 创建设置cglib拦截器 */
        Enhancer.registerCallbacks(proxyClass, new Callback[]{new AsyncMethodInterceptor(target)});
        Object proxyObject = null;
        try {
            proxyObject = ReflectionHelper.newInstance(proxyClass);
        } finally {
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }
        return proxyObject;
    }

}
