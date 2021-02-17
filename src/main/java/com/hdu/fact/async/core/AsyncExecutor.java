/**
 * Copyright (c) 2006-2016 Hzins Ltd. All Rights Reserved.
 * <p>
 * This code is the confidential and proprietary information of
 * Hzins. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Hzins,http://www.hzins.com.
 */
package com.hdu.fact.async.core;

import com.hdu.fact.async.exception.AsyncException;
import com.hdu.fact.async.inject.TransactionBuilder;
import com.hdu.fact.async.pool.AsyncTaskThreadPool;
import com.hdu.fact.async.pool.NamedThreadFactory;
import com.hdu.fact.async.pool.RunnableAround;
import com.hdu.fact.async.util.ReflectionHelper;
import com.hdu.fact.async.bean.AsyncMethod;
import com.hdu.fact.async.bean.AsyncRetry;
import com.hdu.fact.async.cache.AsyncProxyCache;
import com.hdu.fact.async.config.AsyncConfigurer;
import com.hdu.fact.async.config.DefaultAsyncConfigurer;
import com.hdu.fact.async.config.ThreadPoolConfiguration;
import com.hdu.fact.async.constant.HandleMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * 读取配置
 * </p>
 *
 * @author zhou
 */
public final class AsyncExecutor {

    private final static Logger logger = LoggerFactory.getLogger(AsyncExecutor.class);

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicBoolean destroyed = new AtomicBoolean(false);

    private static AsyncTaskThreadPool threadPool;
    private static TransactionBuilder transactionBuilder;

    /**
     * 检查配置文件
     * @param configuration
     */
    public static void checkArgument(ThreadPoolConfiguration configuration) {
        Assert.notNull(configuration, "thread pool configuration propertie not be null");
        Assert.notNull(configuration.getAllowCoreThreadTimeout(), "configuration propertie async.allowCoreThreadTimeout not be null");
        Assert.notNull(configuration.getCorePoolSize(), "configuration propertie async.allowCoreThreadTimeout not be null");
        Assert.notNull(configuration.getKeepAliveTime(), "configuration propertie async.allowCoreThreadTimeout not be null");
        Assert.notNull(configuration.getMaxAcceptCount(), "configuration propertie async.allowCoreThreadTimeout not be null");
        Assert.notNull(configuration.getMaxPoolSize(), "configuration propertie async.allowCoreThreadTimeout not be null");
        Assert.hasText(configuration.getRejectedExecutionHandler(), "configuration propertie async.allowCoreThreadTimeout not be null");
    }

    public static void initializeThreadPool(ThreadPoolConfiguration threadPoolConfiguration) {
        checkArgument(threadPoolConfiguration);
        initializeThreadPool(threadPoolConfiguration.getCorePoolSize(), threadPoolConfiguration.getMaxPoolSize(), threadPoolConfiguration.getMaxAcceptCount(),
                threadPoolConfiguration.getRejectedExecutionHandler(), threadPoolConfiguration.getKeepAliveTime(), threadPoolConfiguration.getAllowCoreThreadTimeout());
    }

    /**
     * 对线程池初始化
     * @param corePoolSize
     * @param maxPoolSize
     * @param maxAcceptCount
     * @param rejectedExecutionHandler
     * @param keepAliveTime
     * @param allowCoreThreadTimeout
     */
    private static void initializeThreadPool(Integer corePoolSize, Integer maxPoolSize, Integer maxAcceptCount, String rejectedExecutionHandler,
                                             Long keepAliveTime, Boolean allowCoreThreadTimeout) {

        if (!initialized.get()) {
            initialized.set(true);
            HandleMode handleMode = HandleMode.CALLERRUN;
            if (StringUtils.hasText(rejectedExecutionHandler)) {
                if (!HandleMode.REJECT.toString().equals(rejectedExecutionHandler) && !HandleMode.CALLERRUN.toString().equals(rejectedExecutionHandler)) {
                    throw new IllegalArgumentException("Invalid configuration properties async.rejectedExecutionHandler");
                }
                if (HandleMode.REJECT.toString().equals(rejectedExecutionHandler)) {
                    handleMode = HandleMode.REJECT;
                }
            }
            RejectedExecutionHandler handler = getRejectedHandler(handleMode);
            BlockingQueue<Runnable> queue = createQueue(maxAcceptCount);
            threadPool = new AsyncTaskThreadPool(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, queue, handler, new NamedThreadFactory());
            threadPool.getThreadPoolExecutor().allowCoreThreadTimeOut(allowCoreThreadTimeout);
            logger.info("ThreadPoolExecutor initialize info corePoolSize:{} maxPoolSize:{} maxAcceptCount:{} rejectedExecutionHandler:{}", corePoolSize, maxPoolSize, maxAcceptCount, handleMode);
        }
    }

    /**
     * 无返回值的方法
     * @param task
     * @param <T>
     */
    public static <T> void execute(AsyncCallable<T> task) {
        submit(task);
    }

    public static <T> AsyncFutureTask<T> submit(AsyncFutureCallable<T> callable) {
        return submit(callable, null);
    }

    public static <T> AsyncFutureTask<T> submit(AsyncFutureCallable<T> callable, AsyncFutureCallback<T> callback) {
        /**如果没有建立线程池，先建立线程池*/
        if (!initialized.get()) {
            AsyncConfigurer asyncConfigurer = new DefaultAsyncConfigurer();
            ThreadPoolConfiguration threadPoolConfiguration = new ThreadPoolConfiguration();
            asyncConfigurer.configureThreadPool(threadPoolConfiguration);
            initializeThreadPool(threadPoolConfiguration);
        }
        AsyncMethod method = buildAsyncMethod(callable);
        if (callable instanceof TransactionCallable) {
            callable = executeTransaction(callable);
        }
        return threadPool.submit(callable, callback, method);
    }


    public static void destroy() throws Exception {
        if (initialized.get() && (threadPool != null)) {
            threadPool.destroy();
            threadPool = null;
        }
    }

    public static <T> AsyncCallable<T> executeTransaction(final AsyncFutureCallable<T> callable) {
        if (transactionBuilder == null) {
            throw new AsyncException("you should integration spring transaction");
        }
        return new AsyncCallable<T>() {
            @Override
            public T doAsync() {
                return transactionBuilder.execute(callable);
            }
        };
    }

    private static <T> AsyncMethod buildAsyncMethod(AsyncFutureCallable<T> callable) {
        if (callable.cacheKey() != null) {
            AsyncMethod method = AsyncProxyCache.getAsyncMethod(callable.cacheKey());
            if (method != null) {
                return method;
            }
        }
        AsyncMethod method = new AsyncMethod(null, null, callable.timeout(), new AsyncRetry(callable.maxAttemps(), callable.exceptions()));
        /** 获取Callable对应的泛型*/
        Class<?> returnClass = ReflectionHelper.getGenericClass(callable.getClass());
        if (Void.TYPE.isAssignableFrom(returnClass) || Void.class.equals(returnClass)) {
            method.setVoid(true);
        }
        return method;
    }


    private static BlockingQueue<Runnable> createQueue(int acceptCount) {
        if (acceptCount > 0) {
            return new LinkedBlockingQueue<>(acceptCount);
        } else {
            return new SynchronousQueue<>();
        }
    }

    private static RejectedExecutionHandler getRejectedHandler(HandleMode mode) {
        return HandleMode.REJECT == mode ? new ThreadPoolExecutor.AbortPolicy() : new ThreadPoolExecutor.CallerRunsPolicy();
    }

    public static boolean isDestroyed() {
        return destroyed.get();
    }

    public static void setIsDestroyed(boolean isDestroyed) {
        AsyncExecutor.destroyed.set(true);
    }

    public static void setTransactionBuilder(TransactionBuilder transactionBuilder) {
        AsyncExecutor.transactionBuilder = transactionBuilder;
    }

    public static void setRunnableAround(RunnableAround runnableAround) {
        if (threadPool != null) {
            threadPool.setRunnableAround(runnableAround);
        }
    }

    public static AsyncTaskThreadPool getAsyncTaskThreadPool() {
        return threadPool;
    }
}
