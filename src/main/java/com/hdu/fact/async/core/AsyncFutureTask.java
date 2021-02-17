package com.hdu.fact.async.core;

import com.hdu.fact.async.util.ReflectionHelper;
import com.hdu.fact.async.bean.AsyncMethod;
import com.hdu.fact.async.processor.AsyncCallbackProcessor;
import com.hdu.fact.async.processor.AsyncRetryProcessor;
import com.hdu.fact.async.processor.RetryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */
public class AsyncFutureTask<V> extends FutureTask<V> {

    private final static Logger logger = LoggerFactory.getLogger(AsyncFutureTask.class);

    private long startTime = 0;

    private long endTime = 0;

    private volatile V result;

    private int counter;

    private AsyncMethod method;

    private Callable<V> callable;

    private AsyncFutureCallback<V> futureCallback;

    private final ReentrantLock mainLock = new ReentrantLock();

    private final Condition condition = mainLock.newCondition();

    public AsyncFutureTask(Callable<V> callable, AsyncFutureCallback<V> futureCallback, AsyncMethod method) {
        super(callable);
        this.callable = callable;
        this.method = method;
        counter = AsyncCounter.intValue();
        if (futureCallback != null) {
            this.futureCallback = futureCallback;
        }
    }

    /**
     * 重写模板方法
     * 在唤醒所有节点后改写的方法
     * 负责开启回调函数
     */
    @Override
    protected void done() {
        endTime = System.currentTimeMillis();
        if (counter >= 0) {
            AsyncCounter.release();
        }
        RetryResult<V> result = new RetryResult<V>();

        /** 如果被取消，执行 onFail() 函数 */
        if (super.isCancelled()) {
            AsyncCallbackProcessor.doCallback(futureCallback, result.setThrowable(new TimeoutException()));
            return;
        }

        /** 如果需要执行毁掉方法或者重试时 */
        if (needCallbackAndRetry()) {
            try {
                /** 设置AsyncFutureTask的value和当前result的值
                 * 取出结果时会发生错误 */
                this.result = innerGetValue(method.getTimeout(), TimeUnit.MILLISECONDS);
                result.setData(this.result);
            } catch (Throwable e) {
                /** 如果发生错误 重试*/
                result.setThrowable(e);

                if (method.isVoid() || getMaxAttemps() > 0) {
                    logger.error("future invoke error", ReflectionHelper.getThrowableCause(e));
                }
                /** 重试处理器*/
                result = AsyncRetryProcessor.handler(callable, method);
                this.result = result.getData();
            } finally {
                if (needLock()) {
                    final ReentrantLock mainLock = this.mainLock;
                    mainLock.lock();
                    try {
                        condition.signal();
                    } finally {
                        mainLock.unlock();
                    }
                }
                /** 触发回调函数*/
                AsyncCallbackProcessor.doCallback(futureCallback, result);
            }
        }
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        if (counter >= 0) {
            AsyncCounter.set(++counter);
        }
        super.run();
    }

    /**
     * 同步运行
     */
    public void syncRun() {
        counter = -1;
        run();
    }

    /**
     * 获取结果，如果 value == null 则让出锁进入阻塞状态
     * 先用时间加锁，如果value还是null说明就是超时
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public V getValue(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        timeout = method.getTimeout();
        if (needCallbackAndRetry()) {
            if (needLock()) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    if (result == null) {
                        if (timeout > 0) {
                            /** 让出锁，进入阻塞状态*/
                            condition.await(timeout, unit);
                        } else {
                            condition.await();
                        }
                    }
                } finally {
                    mainLock.unlock();
                }
                /** 如果还是获取不到，就说明超时了*/
                if (result == null) {
                    throw new TimeoutException();
                }
            }
        }
        return innerGetValue(timeout, unit);
    }

    /**
     * 获取Future的内部结果
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private V innerGetValue(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        if (result == null) {
            long startRunTime = System.currentTimeMillis();
            /** 如果需要*/
            if (timeout <= 0) {
                result = super.get();
            } else {
                result = super.get(timeout, unit);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("invoking time:{} load time:{} timeout:{}", this.endTime - this.startTime, System.currentTimeMillis() - startRunTime, timeout);
            }
        }
        return result;
    }

    private boolean needLock() {
        if (method.getTimeout() > 0 && !method.isVoid() && getMaxAttemps() > 0) {
            return true;
        }
        return false;
    }

    private boolean needCallbackAndRetry() {
        if (futureCallback != null || getMaxAttemps() > 0 || method.isVoid()) {
            return true;
        }
        return false;
    }

    public int getMaxAttemps() {
        if (method.getRetry() == null) {
            return 0;
        }
        return method.getRetry().getMaxAttemps();
    }

    public int getCounter() {
        return counter;
    }


}
