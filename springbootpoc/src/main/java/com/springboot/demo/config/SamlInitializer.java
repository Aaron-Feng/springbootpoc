package com.springboot.demo.config;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class SamlInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    static final Logger logger = LoggerFactory.getLogger("security");
    
    @Override
    public void initialize(ConfigurableApplicationContext appContext) {
        String samlDisable = appContext.getEnvironment().getProperty("auth.saml.disable");
        
        if (!StringUtils.isEmpty(samlDisable) && "true".equals(samlDisable)) {
            return;
        }

        appContext.getEnvironment().addActiveProfile("saml-authentication");

    }

}