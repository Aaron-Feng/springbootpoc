package com.springboot.demo.config;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

// TODO:  This class should move to a UI module

@Configuration
@EnableWebMvc
public class AuthWebConfig extends WebMvcConfigurerAdapter {
    
    @Value("${csf.mvc.default.pagesize:25}")
    int defaultPageSize;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "PATCH");
    }
    
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
           configurer.enable(); // allows static content (.css, .js, etc)
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**/*.js", "/**/*.css", "/**/fonts/**", "/**/images/**").addResourceLocations("/").setCachePeriod(43200); //cache for 12 hours
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setFallbackPageable(new PageRequest(0, defaultPageSize));
        argumentResolvers.add(resolver);
        super.addArgumentResolvers(argumentResolvers);
    }
}