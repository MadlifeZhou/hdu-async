package com.hdu.fact.async.config;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author zhou
 */
public class ExecutorConfiguration {

    @Value("${async.traced:false}")
    private Boolean traced;

    public Boolean getTraced() {
        return traced;
    }

    public void setTraced(Boolean traced) {
        this.traced = traced;
    }

}
 