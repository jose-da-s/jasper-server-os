package com.jaspersoft.jasperserver.api.security.externalAuth.provider;

import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoExternalUserLoginEvent;
import com.jaspersoft.jasperserver.api.metadata.user.service.ExternalUserLoginEventService;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.ExternalUserLoginEventServiceImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserAuthorityServiceImpl;
import com.jaspersoft.jasperserver.api.security.externalAuth.ldap.JSLdapContextSource;
import com.jaspersoft.jasperserver.api.security.externalAuth.wrappers.spring.ldap.JSBindAuthenticator;
import com.jaspersoft.jasperserver.api.security.externalAuth.wrappers.spring.ldap.JSLdapAuthenticationProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class JSLdapAuthenticationProviderTest {

    private String principal = "test-dev-user";
    private String credentials = "test-dev-user";
    private String ldapUrl = "ldap://auth-ldap.jaspersoft.com/dc=eng-infra,dc=jaspersoft,dc=com";
    private String ldapUsername = "cn=ldap-query,ou=Users,dc=eng-infra,dc=jaspersoft,dc=com";
    private String ldapPassword = "ldap-query";
    private JSLdapAuthenticationProvider jsLdapAuthenticationProvider;
    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
    private ExternalUserLoginEventService externalUserLoginEventService;
    private UserDetailsService userDetailsService;
    HttpServletRequest mockHttpServletRequest;
    HttpServletResponse mockHttpServletResponse;
    RequestAttributes requestAttributes;
    LdapAuthenticationProvider ldapAuthenticationProvider;


    @Before
    public void init(){
        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(getPrincipal(),getCredentials());
        ldapAuthenticationProvider = Mockito.mock(LdapAuthenticationProvider.class);
        JSLdapContextSource jsLdapContextSource = new JSLdapContextSource(getLdapUrl());
        jsLdapContextSource.setUserDn(getLdapUsername());
        jsLdapContextSource.setPassword(getLdapPassword());
        JSBindAuthenticator jsBindAuthenticator = new JSBindAuthenticator(jsLdapContextSource);
        jsLdapAuthenticationProvider = Mockito.spy(new JSLdapAuthenticationProvider(jsBindAuthenticator));
        externalUserLoginEventService = Mockito.mock(ExternalUserLoginEventServiceImpl.class);
        jsLdapAuthenticationProvider.setExternalUserLoginEventService(externalUserLoginEventService);
        userDetailsService = Mockito.mock(UserAuthorityServiceImpl.class);
        jsLdapAuthenticationProvider.setUserDetailsService(userDetailsService);
        mockHttpServletRequest= new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
        requestAttributes = new ServletRequestAttributes(mockHttpServletRequest,mockHttpServletResponse);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        jsLdapAuthenticationProvider.setQualifiedNameSeparator("|");

    }

    @Test
    public void testAuthenticateWithValidCredentials(){
        RepoExternalUserLoginEvent repoExternalUserLoginEvent = getRepoExternalUserLoginEvent(getPrincipal());
        Mockito.lenient().doReturn(repoExternalUserLoginEvent).when(externalUserLoginEventService).getExternalUserLoginEventByUsernameAndTenantId(Mockito.anyString(),Mockito.anyString());
        Mockito.doReturn(usernamePasswordAuthenticationToken).when((LdapAuthenticationProvider)jsLdapAuthenticationProvider).authenticate(usernamePasswordAuthenticationToken);
        Mockito.lenient().doReturn(null).when(userDetailsService).loadUserByUsername(getPrincipal());
        Mockito.lenient().doNothing().when(externalUserLoginEventService).updateExternalUserLoginEvent(repoExternalUserLoginEvent);
        Mockito.lenient().doNothing().when(externalUserLoginEventService).addNewExternalUserLoginEvent(repoExternalUserLoginEvent);
        Authentication authentication = new UsernamePasswordAuthenticationToken(getPrincipal(),getCredentials());
        Mockito.when(((AbstractLdapAuthenticationProvider)jsLdapAuthenticationProvider).authenticate(usernamePasswordAuthenticationToken)).thenReturn(authentication);
        Authentication authenticationByLdap = jsLdapAuthenticationProvider.authenticate(usernamePasswordAuthenticationToken);
        Assert.assertNotNull(authenticationByLdap);
        Assert.assertEquals(authenticationByLdap.getPrincipal().toString(),authentication.getPrincipal().toString());
    }

    @Test
    public void testAuthenticateMethodWhenUserStatusIsDisabled(){
        RepoExternalUserLoginEvent repoExternalUserLoginEvent = getRepoExternalUserLoginEvent(getPrincipal());
        repoExternalUserLoginEvent.setEnabled(false);
        Mockito.lenient().doReturn(repoExternalUserLoginEvent).when(externalUserLoginEventService).getExternalUserLoginEventByUsernameAndTenantId(Mockito.anyString(),Mockito.anyString());
        Authentication authentication = new UsernamePasswordAuthenticationToken(getPrincipal(),getCredentials());
        Mockito.when(((AbstractLdapAuthenticationProvider)jsLdapAuthenticationProvider).authenticate(usernamePasswordAuthenticationToken)).thenReturn(authentication);
        Authentication authenticationByLdap = jsLdapAuthenticationProvider.authenticate(authentication);
        HttpSession httpSession = mockHttpServletRequest.getSession();
        Assert.assertEquals(true, httpSession.getAttribute("isUserLocked"));
        Assert.assertFalse(authenticationByLdap.isAuthenticated());
    }

    @Test
    public void testAuthenticateMethodForFirstTimeLogInByExternalUser(){
        RepoExternalUserLoginEvent repoExternalUserLoginEvent = getRepoExternalUserLoginEvent(getPrincipal());
        Mockito.lenient().doReturn(null).when(externalUserLoginEventService).getExternalUserLoginEventByUsernameAndTenantId(Mockito.anyString(),Mockito.anyString());
        Mockito.lenient().doReturn(usernamePasswordAuthenticationToken).
                when((LdapAuthenticationProvider)jsLdapAuthenticationProvider).
                authenticate(usernamePasswordAuthenticationToken);
        Mockito.lenient().doReturn(null).when(userDetailsService).loadUserByUsername(getPrincipal());
        Mockito.lenient().doNothing().when(externalUserLoginEventService).addNewExternalUserLoginEvent(repoExternalUserLoginEvent);
        Authentication authentication = new UsernamePasswordAuthenticationToken(getPrincipal(),getCredentials());
        Mockito.when(((AbstractLdapAuthenticationProvider)jsLdapAuthenticationProvider).authenticate(usernamePasswordAuthenticationToken)).thenReturn(authentication);
        Authentication authenticationByLdap = jsLdapAuthenticationProvider.authenticate(usernamePasswordAuthenticationToken);
        Assert.assertNotNull(authenticationByLdap);
        Assert.assertEquals(authenticationByLdap.getPrincipal().toString(),authentication.getCredentials().toString());
    }

    @Test
    public void testAuthenticateMethodWhenNofaIsDisabled(){
        Mockito.doReturn(usernamePasswordAuthenticationToken).when((LdapAuthenticationProvider)jsLdapAuthenticationProvider).authenticate(usernamePasswordAuthenticationToken);
        Authentication authentication = new UsernamePasswordAuthenticationToken(getPrincipal(),getCredentials());
        Mockito.when(((AbstractLdapAuthenticationProvider)jsLdapAuthenticationProvider).authenticate(usernamePasswordAuthenticationToken)).thenReturn(authentication);
        Authentication authenticationByLdap = jsLdapAuthenticationProvider.authenticate(usernamePasswordAuthenticationToken);
        Assert.assertNotNull(authenticationByLdap);
        Assert.assertEquals(authenticationByLdap.getPrincipal().toString(),authentication.getCredentials().toString());
    }

    private RepoExternalUserLoginEvent getRepoExternalUserLoginEvent(String username) {
        RepoExternalUserLoginEvent repoExternalUserLoginEvent = new RepoExternalUserLoginEvent();
        repoExternalUserLoginEvent.setUsername(username);
        repoExternalUserLoginEvent.setRecordCreationDate(new Date());
        repoExternalUserLoginEvent.setEnabled(true);
        return repoExternalUserLoginEvent;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public String getLdapUsername() {
        return ldapUsername;
    }

    public void setLdapUsername(String ldapUsername) {
        this.ldapUsername = ldapUsername;
    }

    public String getLdapPassword() {
        return ldapPassword;
    }

    public void setLdapPassword(String ldapPassword) {
        this.ldapPassword = ldapPassword;
    }
}
