package com.springboot.demo.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SecurityProfileConditional implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        for (String profile : context.getEnvironment().getActiveProfiles()) {
            /* Returns true if any active profile has term "saml" */
            if (StringUtils.containsIgnoreCase(profile, "saml")) {
                return false;
            }
        }
        return true;
    }

}