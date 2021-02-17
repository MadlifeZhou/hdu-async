package com.hdu.fact.async.inject;

import com.hdu.fact.async.util.CommonUtil;
import com.hdu.fact.async.annotation.Async;
import com.hdu.fact.async.constant.AsyncConstant;
import com.hdu.fact.async.core.AsyncExecutor;
import com.hdu.fact.async.template.AsyncTemplate;
import com.hdu.fact.async.util.ReflectionHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;

/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */
public class SpringBeanPostProcessor implements BeanPostProcessor, Ordered {


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return processAasynBean(bean, beanName);
    }

    public Object processAasynBean(Object bean, String beanName) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        /** 如果没有方法直接返回*/
        if (methods == null || methods.length == 0) {
            return bean;
        }
        for (Method method : methods) {
            Async annotation = ReflectionHelper.findAsyncAnnotation(bean, method);
            // 只要有一个方法标注了，都会建立
            if (annotation != null) {
                return AsyncTemplate.getAsyncProxy(AsyncTemplate.ProxyType.CGLIB).buildProxy(bean, AsyncConstant.ASYNC_DEFAULT_TIME_OUT, false);
            }
        }
        if (CommonUtil.getClass(bean).isAssignableFrom(TransactionBuilder.class)) {
            AsyncExecutor.setTransactionBuilder((TransactionBuilder) bean);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
 