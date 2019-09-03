package com.springboot.demo.config;
import java.util.Date;

import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;

public class SAMLAuthenticationProviderExt extends SAMLAuthenticationProvider {

    @Override
    protected Date getExpirationDate(SAMLCredential credential) {
        return null;
    }
}
