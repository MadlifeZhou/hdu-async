package com.hdu.fact.async.config;

import com.hdu.fact.async.constant.AsyncConstant;
import com.hdu.fact.async.core.AsyncExecutor;
import com.hdu.fact.async.inject.SpringBeanPostProcessor;
import com.hdu.fact.async.inject.TransactionBuilder;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * @author zhou
 */
@Configuration
@Import({ThreadPoolConfiguration.class,
        ExecutorConfiguration.class})
public class BootstrapConfiguration {

    @Autowired
    ThreadPoolConfiguration threadPoolConfiguration;

    @Autowired
    ExecutorConfiguration executorConfiguration;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initialize() throws Exception {
        Map<String, AsyncConfigurer> configurerBeanMaps = applicationContext.getBeansOfType(AsyncConfigurer.class);
        AsyncConfigurer asyncConfigurer = null;
        if (CollectionUtils.isEmpty(configurerBeanMaps)) {
            asyncConfigurer = new DefaultAsyncConfigurer();
        } else {
            if (configurerBeanMaps.size() > 1) {
                throw new NoUniqueBeanDefinitionException(AsyncConfigurer.class, configurerBeanMaps.size(), "Multiple beans found among candidates: " + configurerBeanMaps.keySet());
            }
            asyncConfigurer = configurerBeanMaps.entrySet().iterator().next().getValue();
        }

        if (asyncConfigurer == null) {
            asyncConfigurer = new DefaultAsyncConfigurer();
        }
        asyncConfigurer.configureExecutorConfiguration(executorConfiguration);
        asyncConfigurer.configureThreadPool(threadPoolConfiguration);
        AsyncExecutor.initializeThreadPool(threadPoolConfiguration);
        if (asyncConfigurer.getRunnableAround() != null) {
            AsyncExecutor.setRunnableAround(asyncConfigurer.getRunnableAround());
        }
        AsyncConstant.ASYNC_DEFAULT_TRACE_LOG = executorConfiguration.getTraced();
    }

    @Bean
    public SpringBeanPostProcessor springBeanPostProcessor() {
        return new SpringBeanPostProcessor();
    }

    @Bean
    public TransactionBuilder transactionBuilder() {
        return new TransactionBuilder();
    }

    @PreDestroy
    public void destory() throws Exception {
        AsyncExecutor.setIsDestroyed(true);
        AsyncExecutor.destroy();
    }

}
 