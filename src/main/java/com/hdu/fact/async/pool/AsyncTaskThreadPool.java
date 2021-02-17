package com.hdu.fact.async.pool;

import com.hdu.fact.async.bean.AsyncMethod;
import com.hdu.fact.async.core.AsyncFutureCallback;
import com.hdu.fact.async.core.AsyncFutureTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * <p>
 * Async框架中的封装ThreadPool
 * </p>
 *
 * @author zhou
 */
public final class AsyncTaskThreadPool {

    private static Logger logger = LoggerFactory.getLogger(AsyncTaskThreadPool.class);
     /** 定义线程池类*/
    private ThreadPoolExecutor executor = null;

    private RunnableAround runnableAround;

    private int corePoolSize;

    /**
     * 线程池构造函数
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     */
    public AsyncTaskThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue) {
        this.corePoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public AsyncTaskThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this.corePoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public AsyncTaskThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler, ThreadFactory threadFactory) {
        this.corePoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /**
     * 线程池的submit方法
     * @param callable
     * @param callback
     * @param method
     * @param <T>
     * @return
     */
    public <T> AsyncFutureTask<T> submit(Callable<T> callable, AsyncFutureCallback<T> callback, AsyncMethod method) {
        if (callable == null) {
            throw new NullPointerException();
        }

        /** 新建一个FutureTask*/
        AsyncFutureTask<T> futureTask = new AsyncFutureTask<T>(callable, callback, method);

        /** 如果核心线程数小于等于正在运行的数量，就转为同步*/
        if (futureTask.getCounter() > 0 && corePoolSize <= executor.getActiveCount()) {
            futureTask.syncRun();
            return futureTask;
        }
        /** 调用线程池的execute函数*/
        execute(futureTask);
        return futureTask;
    }

    private void execute(Runnable command) {
        if (runnableAround != null) {
            command = runnableAround.advice(command);
        }
        executor.execute(command);
    }

    /**
     * 线程池结束方法
     * @throws Exception
     */
    public void destroy() throws Exception {
        if (!executor.isShutdown()) {
            executor.shutdown();
            boolean loop = true;
            do {
                loop = executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
                logger.info("Wait for the async thread to finish the work; The remaining queue size: {}", executor.getQueue().size());
            } while (!loop);
            logger.info("AsyncThreadTaskPool destroyed {}", executor.toString());
            executor = null;
        }
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return executor;
    }

    public void setRunnableAround(RunnableAround runnableAround) {
        this.runnableAround = runnableAround;
    }

}
 