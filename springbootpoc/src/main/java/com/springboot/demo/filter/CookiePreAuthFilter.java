package com.springboot.demo.filter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.adp.domain.UserDetails;

@Component
public class CookiePreAuthFilter extends AbstractPreAuthenticatedProcessingFilter {
    
    public CookiePreAuthFilter() {
        setCheckForPrincipalChanges(true);
        setInvalidateSessionOnPrincipalChange(true);
    }
    
    @Autowired
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
    
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "";
    }
    
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();    
        Cookie cookie = WebUtils.getCookie(request, "profileid");
        
        boolean authenticated = (currentUser != null && currentUser.isAuthenticated());
        
        if (cookie != null) {
            String token = "cookie:" + cookie.getValue();
            
            Cookie actCookie = WebUtils.getCookie(request, "actualprofileid");
            if (actCookie != null) {
                token += ":" + actCookie.getValue();
            }
            
            if (authenticated) {
                UserDetails details = (UserDetails) currentUser.getPrincipal();
                if (details != null && token.equals(details.getToken())) {
                    return currentUser.getName();
                }
            }
            logger.debug("Authenticating new or changed cookie");
            return token;
        }
        
        if (authenticated) {
            UserDetails details = (UserDetails) currentUser.getPrincipal();
            if (details != null && details.getToken() != null && !details.getToken().startsWith("cookie:")) {
                return currentUser.getName();
            }
        }
        
        return null;
    }
}