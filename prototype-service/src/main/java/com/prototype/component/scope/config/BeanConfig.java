package com.prototype.component.scope.config;

import com.prototype.component.scope.service.ScopeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class BeanConfig {

    @Bean
    @Scope("singleton")
    public ScopeService singletonService() {
        return new ScopeService("singleton service");
    }

    @Bean
    @Scope("prototype")
    public ScopeService prototypeService() {
        return new ScopeService("prototype service");
    }

}
