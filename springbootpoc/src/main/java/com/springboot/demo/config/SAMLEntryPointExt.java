package com.springboot.demo.config;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class SAMLEntryPointExt extends SAMLEntryPoint {

    private static Logger logger = LoggerFactory.getLogger("security");
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        Boolean isAuthenticated = false;
        SecurityContext context = (SecurityContext) request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        if (context != null && context.getAuthentication() != null) {
            isAuthenticated = true;
        }

        logger.debug("Setting snetAuthenticated to " + isAuthenticated + " for " + request.getRequestURL());
        response.addHeader("snetAuthenticated", isAuthenticated.toString());
        
        //For secured content api if rest end point is not authenticated saml redirection won't occur 
        if((request.getRequestURL()).toString().contains("/content/allowed")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        else
            super.commence(request, response, e);
    }
    
    

}
