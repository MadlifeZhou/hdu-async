package com.hdu.fact.async.annotation;

import com.hdu.fact.async.config.BootstrapConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author zhou
 */
@Configuration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({BootstrapConfiguration.class})
public @interface EnableAsync {

    boolean proxyTargetClass() default false;

}
 