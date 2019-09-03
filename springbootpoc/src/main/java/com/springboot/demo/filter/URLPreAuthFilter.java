package com.springboot.demo.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Component;

import com.adp.domain.UserDetails;

@Component
public class URLPreAuthFilter extends AbstractPreAuthenticatedProcessingFilter {
    
    public URLPreAuthFilter() {
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
        String username = request.getParameter("user");
        
        boolean authenticated = (currentUser != null && currentUser.isAuthenticated());
        
        if (username == null) {
            if (authenticated) {
                return currentUser.getName();
            }
            return null;
        }
        
        String token = "username:" + username;
        if (!authenticated) {
            logger.debug("Authenticating username (URL): " + username);
            return token;
        }

        UserDetails details = (UserDetails) currentUser.getPrincipal();
        if (token.equals(details.getToken())) {
            return details.getUsername();
        }
        
        logger.debug("Authenticating new username (URL): " + username);
        return token;
    }
}