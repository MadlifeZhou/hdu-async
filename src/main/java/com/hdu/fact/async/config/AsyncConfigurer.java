package com.hdu.fact.async.config;


import com.hdu.fact.async.pool.RunnableAround;


/**
 * @author zhou
 */
public interface AsyncConfigurer {

    void configureExecutorConfiguration(ExecutorConfiguration configuration);

    void configureThreadPool(ThreadPoolConfiguration configuration);

    RunnableAround getRunnableAround();

}