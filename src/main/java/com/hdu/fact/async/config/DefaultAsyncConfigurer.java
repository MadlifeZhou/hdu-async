package com.hdu.fact.async.config;

import com.hdu.fact.async.constant.AsyncConstant;
import com.hdu.fact.async.pool.RunnableAround;
import com.hdu.fact.async.constant.HandleMode;
import org.springframework.util.StringUtils;


/**
 * @author zhou
 */
public class DefaultAsyncConfigurer implements AsyncConfigurer {

    @Override
    public void configureExecutorConfiguration(ExecutorConfiguration configuration) {
        if (configuration == null) {
            return;
        }
        configuration.setTraced(false);
    }

    @Override
    public RunnableAround getRunnableAround() {
        return null;
    }

    @Override
    public void configureThreadPool(ThreadPoolConfiguration configuration) {
        if (configuration == null) {
            return;
        }
        Integer corePoolSize = configuration.getCorePoolSize();
        Integer maxPoolSize = configuration.getMaxPoolSize();
        Integer maxAcceptCount = configuration.getMaxAcceptCount();
        Long keepAliveTime = configuration.getKeepAliveTime();
        Boolean allowCoreThreadTimeout = configuration.getAllowCoreThreadTimeout();
        String rejectedExecutionHandler = configuration.getRejectedExecutionHandler();

        if (!StringUtils.hasText(rejectedExecutionHandler)) {
            rejectedExecutionHandler = HandleMode.CALLERRUN.toString();
        } else {
            if (!HandleMode.REJECT.toString().equals(rejectedExecutionHandler) && !HandleMode.CALLERRUN.toString().equals(rejectedExecutionHandler)) {
                throw new IllegalArgumentException("Invalid configuration properties async.rejectedExecutionHandler");
            }
        }

        /**
         * 以下部分开启默认分配环节
         */

        /**动态分配cpu核心线程*/
        if (corePoolSize == null || corePoolSize <= 0) {
            corePoolSize = Runtime.getRuntime().availableProcessors() * 4;
        }

        /** 最大线程数*/
        if (maxPoolSize == null || maxPoolSize <= 0) {
            maxPoolSize = corePoolSize * 2;
        }

        if (maxAcceptCount == null || maxAcceptCount < 0) {
            maxAcceptCount = corePoolSize;
        }

        if (keepAliveTime == null || keepAliveTime < 0) {
            keepAliveTime = AsyncConstant.ASYNC_DEFAULT_KEEPALIVETIME;
        }

        if (allowCoreThreadTimeout == null) {
            allowCoreThreadTimeout = true;
        }

        configuration.setAllowCoreThreadTimeout(allowCoreThreadTimeout);
        configuration.setCorePoolSize(corePoolSize);
        configuration.setKeepAliveTime(keepAliveTime);
        configuration.setMaxAcceptCount(maxAcceptCount);
        configuration.setMaxPoolSize(maxPoolSize);
        configuration.setRejectedExecutionHandler(rejectedExecutionHandler);
    }

}
 