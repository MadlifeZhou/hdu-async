package com.hdu.fact.async.processor;

import com.hdu.fact.async.core.AsyncFutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhou
 */
public class AsyncCallbackProcessor {

    private final static Logger logger = LoggerFactory.getLogger(AsyncCallbackProcessor.class);

    /**
     * 根据RetryResult中的Exception结果来判断需要执行 onFailure() 还是 onSuccess()
     * @param futureCallback
     * @param result
     * @param <V>
     */
    public static <V> void doCallback(AsyncFutureCallback<V> futureCallback, RetryResult<V> result) {
        if (futureCallback != null) {
            try {
                if (result.getThrowable() != null) {
                    futureCallback.onFailure(result.getThrowable());
                } else {
                    futureCallback.onSuccess(result.getData());
                }
            } catch (Throwable e) {
                logger.error("async callback error", e);
            }
        }
    }

}
