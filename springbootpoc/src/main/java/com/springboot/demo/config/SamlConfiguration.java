package com.springboot.demo.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPArtifactBinding;
import org.springframework.security.saml.processor.HTTPPAOS11Binding;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.HTTPSOAP11Binding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.storage.EmptyStorageFactory;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.saml.trust.httpclient.TLSProtocolSocketFactory;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.ArtifactResolutionProfile;
import org.springframework.security.saml.websso.ArtifactResolutionProfileImpl;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileECPImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.adp.auth.service.PostAuthSNETSAMLUserDetailsService;
import com.adp.auth.service.SAMLUserDetailsService;
import com.adp.util.MetaDataIdUtil;
import com.adp.domain.EnvironmentType;
import com.adp.core.service.URLService;

@Configuration("AuthSamlSpringConfiguration")
@Profile("saml-authentication")
@ComponentScan("org.springframework.security.saml.web")
@EnableGlobalAuthentication
@EnableWebSecurity(debug = false)
@EnableGlobalMethodSecurity(securedEnabled = true)
@PropertySources({
        @PropertySource(value = "classpath:auth-${adp.server.env}.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:/var/snet/resources/properties/auth.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:/srv/snet/resources/properties/auth.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:c:/adp/properties/auth.properties", ignoreResourceNotFound = true) })
public class SamlConfiguration extends WebSecurityConfigurerAdapter implements EnvironmentAware {
    private static Logger logger = LoggerFactory.getLogger("security");

    @Autowired
    private SAMLUserDetailsService SAMLUserDetailsService;

    @Autowired
    private PostAuthSNETSAMLUserDetailsService postAuthSNETSAMLUserDetailsService;

    @Autowired
    private Environment env;
    @Override
    public void setEnvironment(final Environment environment) {
        this.env = environment;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/META-INF/MANIFEST.MF");

        // add defaults to be ignored by spring security
        web.ignoring().antMatchers("/**/*.js", "/**/*.css", "/fonts/**", "/images/**");
    }

    public boolean isLocal() {
        if (env.getActiveProfiles() == null) {
            return true;
        } else if (Arrays.asList(env.getActiveProfiles()).contains(EnvironmentType.PROD.getCode())) {
            return false;
        } else if (Arrays.asList(env.getActiveProfiles()).contains(EnvironmentType.QA.getCode())) {
            return false;
        } else if (Arrays.asList(env.getActiveProfiles()).contains(EnvironmentType.DEVL.getCode())) {
            return false;
        }

        return true;
    }

    private EnvironmentType getEnvironmentType() {

        if (env.getActiveProfiles() == null) {
            return EnvironmentType.LOCAL;
        } else if (Arrays.asList(env.getActiveProfiles()).contains(EnvironmentType.PROD.getCode())) {
            return EnvironmentType.PROD;
        } else if (Arrays.asList(env.getActiveProfiles()).contains(EnvironmentType.QA.getCode())) {
            return EnvironmentType.QA;
        } else if (Arrays.asList(env.getActiveProfiles()).contains(EnvironmentType.DEVL.getCode())) {
            return EnvironmentType.DEVL;
        }

        return EnvironmentType.LOCAL;
    }

    @Autowired
    private URLService urlService;

    public SamlConfiguration() {
        logger.info("Configuring csf-auth module with SAML...");
    }

    // Initialization of the velocity engine
    @Bean
    public VelocityEngine velocityEngine() {
        return VelocityFactory.getEngine();
    }

    // XML parser pool needed for OpenSAML parsing
    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    // Bindings, encoders and decoders used for creating and parsing messages
    @Bean
    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
        return new MultiThreadedHttpConnectionManager();
    }

    @Bean
    public HttpClient httpClient() {
        return new HttpClient(multiThreadedHttpConnectionManager());
    }

    // SAML Authentication Provider responsible for validating of received SAML
    // messages
    @Bean
    public SAMLAuthenticationProviderExt samlAuthenticationProvider() {
        CSFSAMLAuthenticationProvider samlAuthenticationProvider = new CSFSAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(snetSAMLUserDetailsService);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    // Provider of default SAML Context
    @Bean
    public SAMLContextProviderImpl contextProvider() {
        URL url;
        try {
            url = new URL(urlService.getHomeUrl());
        } catch (MalformedURLException mue) {
            logger.error("URL is malformed: " + urlService.getHomeUrl());
            return new SAMLContextProviderImpl();
        }

        SAMLContextProviderLB provider = new SAMLContextProviderLB();
        provider.setScheme(url.getProtocol());
        provider.setServerName(url.getHost());
        provider.setServerPort(url.getPort());
        provider.setStorageFactory(new EmptyStorageFactory());

        if (("http".equals(url.getProtocol()) && url.getPort() == 80)
                || ("https".equals(url.getProtocol()) && url.getPort() == 443)) {
            provider.setIncludeServerPortInRequestURL(false);
        } else {
            provider.setIncludeServerPortInRequestURL(true);
        }
        provider.setContextPath(url.getPath());
        return provider;
    }

    // Initialization of OpenSAML library
    @Bean
    public static SAMLBootstrapExt SAMLBootstrap() {
        return new SAMLBootstrapExt();
    }

    // Logger for SAML messages and events
    @Bean
    public SAMLDefaultLogger samlLogger() {
        return new SAMLDefaultLogger();
    }

    // SAML 2.0 WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        return new WebSSOProfileConsumerImpl();
    }

    // SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    // SAML 2.0 Web SSO profile
    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    // SAML 2.0 Holder-of-Key Web SSO profile
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    // SAML 2.0 ECP profile
    @Bean
    public WebSSOProfileECPImpl ecpprofile() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public SingleLogoutProfile logoutprofile() {
        return new SingleLogoutProfileImpl();
    }

    // Central storage of cryptographic keys
    @Bean
    public KeyManager keyManager() {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader.getResource("classpath:/security/samlKeystore.jks");
        String storePass = "test";
        Map<String, String> passwords = new HashMap<String, String>();
        // passwords.put("apollo", "nalle123");
        passwords.put("ssotest", "test");
        passwords.put("ssotest-local", "test");

        String defaultKey = (isLocal() ? "ssotest-local" : "test");
        return new JKSKeyManager(storeFile, storePass, passwords, defaultKey);
    }

    // Setup TLS Socket Factory
    @Bean
    public TLSProtocolConfigurer tlsProtocolConfigurer() {
        return new TLSProtocolConfigurer();
    }

    @Bean
    public ProtocolSocketFactory socketFactory() {
        return new TLSProtocolSocketFactory(keyManager(), null, "default");
    }

    @Bean
    public Protocol socketFactoryProtocol() {
        return new Protocol("https", socketFactory(), 443);
    }

    @Bean
    public MethodInvokingFactoryBean socketFactoryInitialization() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(Protocol.class);
        methodInvokingFactoryBean.setTargetMethod("registerProtocol");
        Object[] args = { "https", socketFactoryProtocol() };
        methodInvokingFactoryBean.setArguments(args);
        return methodInvokingFactoryBean;
    }

    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        return webSSOProfileOptions;
    }

    // Entry point to initialize authentication, default values taken from
    // properties file
    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        CSFSamlEntryPoint samlEntryPoint = new CSFSamlEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
        return samlEntryPoint;
    }

    // Setup advanced info about metadata
    @Bean
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(false);
        extendedMetadata.setSignMetadata(false);
        return extendedMetadata;
    }

    // IDP Discovery Service
    @Bean
    public SAMLDiscovery samlIDPDiscovery() {
        SAMLDiscovery idpDiscovery = new SAMLDiscovery();
        idpDiscovery.setIdpSelectionPath("/WEB-INF/jsp/idpSelection.jsp");
        return idpDiscovery;
    }

    @Bean
    @Qualifier("idp-oam")
    public ExtendedMetadataDelegate oamExtendedMetadataProvider() throws MetadataProviderException {
        Timer backgroundTaskTimer = new Timer(true);
        ClasspathResource resource;
        try {

            EnvironmentType environmentType = getEnvironmentType();

            if (!environmentType.equals(EnvironmentType.LOCAL) && !environmentType.equals(EnvironmentType.DEVL)) {
                resource = new ClasspathResource("/metadata/oam_fed-" + environmentType.getCode() + ".xml");
            } else {
                resource = new ClasspathResource("/metadata/oam_fed.xml");
            }

            logger.debug("OAM Fed (idp metadata) is " + resource.getLocation());
        } catch (ResourceException e) {
            logger.error("Error loading metadata resource", e);
            throw new MetadataProviderException(e);
        }
        ResourceBackedMetadataProvider provider = new ResourceBackedMetadataProvider(backgroundTaskTimer, resource);
        provider.setParserPool(parserPool());

        ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(provider, extendedMetadata());
        // extendedMetadataDelegate.setMetadataTrustCheck(true);
        extendedMetadataDelegate.setMetadataTrustCheck(false);
        extendedMetadataDelegate.setMetadataRequireSignature(false);

        return extendedMetadataDelegate;
    }

    // IDP Metadata configuration - paths to metadata of IDPs in circle of trust
    // Do no forget to call iniitalize method on providers
    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws MetadataProviderException {
        List<MetadataProvider> providers = new ArrayList<MetadataProvider>();
        providers.add(oamExtendedMetadataProvider());
        CachingMetadataManager manager = new CachingMetadataManager(providers);
        return manager;
    }

    // Filter automatically generates default SP metadata
    @Bean
    public MetadataGenerator metadataGenerator() {
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        metadataGenerator.setEntityId((env.getActiveProfiles() == null
                ? MetaDataIdUtil.ENTITY_ID + MetaDataIdUtil.HYPHEN + EnvironmentType.LOCAL.getCode()
                : MetaDataIdUtil.getMetaDataEntityID(env.getActiveProfiles())));
        metadataGenerator.setEntityBaseURL(urlService.getHomeUrl());
        metadataGenerator.setExtendedMetadata(extendedMetadata());
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setKeyManager(keyManager());
        return metadataGenerator;
    }

    // The filter is waiting for connections on URL suffixed with filterSuffix
    // and presents SP metadata there
    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new MetadataDisplayFilter();
    }

    // Handler deciding where to redirect user after successful login
    /*
     * @Bean public SavedRequestAwareAuthenticationSuccessHandler
     * successRedirectHandler() { SavedRequestAwareAuthenticationSuccessHandler
     * successRedirectHandler = new
     * SavedRequestAwareAuthenticationSuccessHandler();
     * successRedirectHandler.setDefaultTargetUrl("/landing"); return
     * successRedirectHandler; }
     */
    @Bean
    public CSFAuthAuthenticationSuccessHandler successRedirectHandler() {
        CSFAuthAuthenticationSuccessHandler successRedirectHandler = new CSFAuthAuthenticationSuccessHandler();
        successRedirectHandler.setTargetUrlParameter("redirect");
        successRedirectHandler.setDefaultTargetUrl("/");
        return successRedirectHandler;
    }

    // Handler deciding where to redirect user after failed login
    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/error");
        return failureHandler;
    }


    @Bean
    public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
        SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
        samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOHoKProcessingFilter;
    }

    // Processing filter for WebSSO profile messages
    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());

        // HS: Following unfinished/untested code was to get Spring/Tomcat to
        // create a new session post SAML auth vs. migrating a session.
        // SessionFixationProtectionStrategy sessionFixationProtectionStrategy =
        // new SessionFixationProtectionStrategy();
        // sessionFixationProtectionStrategy.setAlwaysCreateSession(true);
        // samlWebSSOProcessingFilter.setSessionAuthenticationStrategy(sessionFixationProtectionStrategy);
        return samlWebSSOProcessingFilter;
    }

    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        return new MetadataGeneratorFilter(metadataGenerator());
    }

    // Handler for successful logout
    public CSFAuthLogoutSuccessHandler successLogoutHandler() {
        CSFAuthLogoutSuccessHandler successLogoutHandler = new CSFAuthLogoutSuccessHandler();
        successLogoutHandler.setDefaultTargetUrl("/");
        return successLogoutHandler;
    }

    /*
     * @Bean public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
     * SimpleUrlLogoutSuccessHandler successLogoutHandler = new
     * SimpleUrlLogoutSuccessHandler();
     * successLogoutHandler.setDefaultTargetUrl("/"); return
     * successLogoutHandler; }
     */

    // Logout handler terminating local session
    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    // Filter processing incoming logout messages
    // First argument determines URL user will be redirected to after successful
    // global logout
    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
    }

    // Overrides default logout processing filter with the one processing SAML
    // messages
    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(), new LogoutHandler[] { logoutHandler() },
                new LogoutHandler[] { logoutHandler() });
    }

    // Bindings
    private ArtifactResolutionProfile artifactResolutionProfile() {
        final ArtifactResolutionProfileImpl artifactResolutionProfile = new ArtifactResolutionProfileImpl(httpClient());
        artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding()));
        return artifactResolutionProfile;
    }

    @Bean
    public HTTPArtifactBinding artifactBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
        return new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile());
    }

    @Bean
    public HTTPSOAP11Binding soapBinding() {
        return new HTTPSOAP11Binding(parserPool());
    }

    @Bean
    public HTTPPostBinding httpPostBinding() {
        return new HTTPPostBinding(parserPool(), velocityEngine());
    }

    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
        return new HTTPRedirectDeflateBinding(parserPool());
    }

    @Bean
    public HTTPSOAP11Binding httpSOAP11Binding() {
        return new HTTPSOAP11Binding(parserPool());
    }

    @Bean
    public HTTPPAOS11Binding httpPAOS11Binding() {
        return new HTTPPAOS11Binding(parserPool());
    }

    // Processor
    @Bean
    public SAMLProcessorImpl processor() {
        Collection<SAMLBinding> bindings = new ArrayList<SAMLBinding>();
        bindings.add(httpRedirectDeflateBinding());
        bindings.add(httpPostBinding());
        bindings.add(artifactBinding(parserPool(), velocityEngine()));
        bindings.add(httpSOAP11Binding());
        bindings.add(httpPAOS11Binding());
        return new SAMLProcessorImpl(bindings);
    }

    /**
     * Define the security filter chain in order to support SSO Auth by using
     * SAML 2.0
     * 
     * @return Filter chain proxy
     * @throws Exception
     */
    @Bean
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
                metadataDisplayFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
                samlWebSSOProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSOHoK/**"),
                samlWebSSOHoKProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
                samlLogoutProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"), samlIDPDiscovery()));
        return new FilterChainProxy(chains);
    }

    /**
     * Returns the authentication manager currently used by Spring. It
     * represents a bean definition with the aim allow wiring from other classes
     * performing the Inversion of Control (IoC).
     * 
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Defines the web based security configuration.
     * 
     * @param http
     *            It allows configuring web based security for specific http
     *            requests.
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().authenticationEntryPoint(samlEntryPoint());
        http.csrf().disable();
        http.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
                .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(switchUserFilter(), FilterSecurityInterceptor.class);
        http.authorizeRequests().antMatchers("/error").permitAll().antMatchers("/appError.html").permitAll()
                .antMatchers("/snetlogout").permitAll().antMatchers("/appLogout.html").permitAll()
                .antMatchers("/saml/**").permitAll().antMatchers("/favicon.ico").permitAll().antMatchers("/css/**")
                .permitAll().antMatchers("/fonts/**").permitAll().antMatchers("/images/**").permitAll()
                .antMatchers("/scripts/**").permitAll().antMatchers("/styles/**").permitAll();

        http.logout().logoutSuccessUrl("/");
    }

    /**
     * Sets a custom authentication provider.
     * 
     * @param auth
     *            SecurityBuilder used to create an AuthenticationManager.
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(samlAuthenticationProvider());
    }

}
