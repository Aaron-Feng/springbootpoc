package com.springboot.demo.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import com.springboot.demo.filter.CookiePreAuthFilter;
import com.springboot.demo.filter.URLPreAuthFilter;
import com.adp.auth.service.UserDetailsService;
import com.springboot.demo.util.SecurityProfileConditional;

@Configuration("AuthConfiguration")
@Conditional(SecurityProfileConditional.class)
@EnableGlobalAuthentication
@EnableWebSecurity
@PropertySources({
    @PropertySource(value = "classpath:auth-${adp.server.env}.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "file:/var/snet/resources/properties/auth.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "file:/srv/snet/resources/properties/auth.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "file:c:/adp/properties/auth.properties", ignoreResourceNotFound = true)
})
public class AuthConfiguration extends WebSecurityConfigurerAdapter {
    static final Logger logger = LoggerFactory.getLogger("security");
    
    @Autowired
    private UserDetailsService UserDetailsService;
    
    @Autowired
    private Environment environment;
    
    private Boolean enableURLAuth;

    @Autowired @Lazy
    public CookiePreAuthFilter cookiePreAuthFilter;
    
    @Autowired @Lazy
    public URLPreAuthFilter urlPreAuthFilter;
    
    
    public void setEnableURLAuth(Boolean enableURLAuth) {
        this.enableURLAuth = enableURLAuth;
    }
    
    public Boolean isEnableURLAuth() {
        return enableURLAuth;
    }
    
    public AuthConfiguration() {
        logger.info("Configuring csf-auth module...");
    }
    
    
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Override
    public void configure(AuthenticationManagerBuilder registry) throws Exception {
        PreAuthenticatedAuthenticationProvider authProvider = new PreAuthenticatedAuthenticationProvider();
        authProvider.setPreAuthenticatedUserDetailsService(UserDetailsService);
        authProvider.setOrder(1);
        
        registry.authenticationProvider(authProvider);
    }
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/META-INF/MANIFEST.MF");
        
        //add defaults to be ignored by spring security
        web.ignoring().antMatchers("/**/*.js", "/**/*.css", "/fonts/**", "/images/**");
        
    }
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .headers().frameOptions().sameOrigin()  
            .and()
            .authorizeRequests()
                .anyRequest().authenticated()
            .and()
            .addFilter(cookiePreAuthFilter)
            .csrf().disable();
        logger.info("CSF cookie authentication enabled");
    
        // If the environment is not PROD, enable the URL filter
        if (environment.getActiveProfiles() == null || !Arrays.asList(environment.getActiveProfiles()).contains(EnvironmentType.PROD.getCode())) {
            logger.info("CSF URL authentication enabled");
            http.addFilterBefore(urlPreAuthFilter, cookiePreAuthFilter.getClass());
        }
        else {
            logger.info("CSF URL authentication disabled in production environment");
        }
    }
}