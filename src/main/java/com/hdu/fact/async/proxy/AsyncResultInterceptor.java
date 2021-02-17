package com.hdu.fact.async.proxy;

import com.hdu.fact.async.constant.AsyncConstant;
import com.hdu.fact.async.core.AsyncFutureTask;
import com.hdu.fact.async.util.CommonUtil;
import com.hdu.fact.async.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 *
 *
 * </p>
 *
 * @author zhou
 */
@SuppressWarnings("all")
public class AsyncResultInterceptor implements MethodInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(AsyncResultInterceptor.class);

    private AsyncFutureTask future;

    private long timeout;

    public AsyncResultInterceptor(AsyncFutureTask future, long timeout) {
        this.future = future;
        this.timeout = timeout;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (AsyncConstant.ASYNC_DEFAULT_TRACE_LOG) {
            logger.debug("start call future:{},object:{} method:{}", future, object.getClass().getName(), CommonUtil.buildMethod(method));
        }
        if (!ReflectionHelper.canProxyInvoke(method)) {
            return ReflectionHelper.invoke(object, args, method);
        }
        /** 把future中的文件加载出来*/
        object = loadFuture();
        if (object != null) {
            logger.info("================return方法代理调用" + method.getName());
            return ReflectionHelper.invoke(object, args, method);
        }
        return null;
    }

    private Object loadFuture() throws Throwable {
        try {
            return future.getValue(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (InterruptedException e) {
            throw e;
        } catch (Throwable e) {
            throw ReflectionHelper.getThrowableCause(e);
        }
    }
}
