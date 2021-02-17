package com.hdu.fact.async.inject;

import com.hdu.fact.async.core.AsyncFutureCallable;
import com.hdu.fact.async.exception.AsyncException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zhou
 * @description 构建 @Transactional 方法
 */
public class TransactionBuilder {

    @Transactional(rollbackFor = Exception.class)
    public <T> T execute(AsyncFutureCallable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new AsyncException(e);
        }
    }

}
