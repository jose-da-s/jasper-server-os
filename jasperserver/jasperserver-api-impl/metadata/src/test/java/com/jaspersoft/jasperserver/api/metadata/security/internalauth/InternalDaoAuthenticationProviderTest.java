package com.jaspersoft.jasperserver.api.metadata.security.internalauth;

import com.jaspersoft.jasperserver.api.metadata.common.service.impl.LogEventService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.LogEventServiceImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoUser;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.ExternalUserLoginEventServiceImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserAuthorityServiceImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserIsNotInternalException;
import com.jaspersoft.jasperserver.api.security.SystemLoggedInUserStorage;
import com.jaspersoft.jasperserver.api.security.internalAuth.InternalDaoAuthenticationProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class InternalDaoAuthenticationProviderTest extends InternalDaoAuthenticationProvider{

    @Mock
    SystemLoggedInUserStorage systemLoggedInUserStorage;

    @Mock
    UserAuthorityServiceImpl userDetailsService;

    @Mock
    LogEventService logEventService;

    private static final String JOE_USER_USERNAME = "joeuser";
    private static final String JOE_USER_PASSWORD = "joeuser";
    private static final String USER_LOCK_ALLOWED_LOGIN_ATTEMPTS = "3";
    private Principal principal;
    private String credentials;
    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
    HttpServletRequest mockHttpServletRequest;
    HttpServletResponse mockHttpServletResponse;
    RequestAttributes requestAttributes;
    static PasswordEncoder passwordEncoder = getPasswordEncoderfortest();

    @Before
    public void init(){
        systemLoggedInUserStorage = Mockito.mock(SystemLoggedInUserStorage.class);
        principal = getPrincipalInstanceForTestUser();
        credentials = getCredentials();
        userDetailsService = Mockito.mock(UserAuthorityServiceImpl.class);
        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(principal,credentials);
        mockHttpServletRequest= new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
        requestAttributes = new ServletRequestAttributes(mockHttpServletRequest,mockHttpServletResponse);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        logEventService = Mockito.mock(LogEventServiceImpl.class);
    }

    @Test
    public void testUserWithValidCredentials(){
        RepoUser repoUser = getRepoUser(InternalDaoAuthenticationProviderTest.JOE_USER_USERNAME,InternalDaoAuthenticationProviderTest.JOE_USER_PASSWORD);
        MetadataUserDetails metadataUserDetails = new MetadataUserDetails(repoUser);
        InternalDaoAuthenticationProviderTest test = Mockito.spy(InternalDaoAuthenticationProviderTest.class);
        test.setUserDetailsService(userDetailsService);
        test.setSystemLoggedInUserStorage(systemLoggedInUserStorage);
        test.setLoggingService(logEventService);
        Mockito.lenient().doNothing().when((UserAuthorityService)userDetailsService).putUser(Mockito.any(),Mockito.any());
        Mockito.lenient().doNothing().when(logEventService).createUserAccountLockedEvent(metadataUserDetails);
        test.additionalAuthenticationChecks(metadataUserDetails,usernamePasswordAuthenticationToken);
        HttpSession httpSession = mockHttpServletRequest.getSession();
        Assert.assertNull(httpSession.getAttribute("numberOfLoginAttemptsRemaining"));
        Assert.assertEquals(Integer.valueOf(0),repoUser.getNumberOfFailedLoginAttempts());
    }

    @Test(expected = BadCredentialsException.class)
    public void testUserWithInvalidCredentials(){
        RepoUser repoUser = getRepoUser("testUser","testUser");
        MetadataUserDetails metadataUserDetails = new MetadataUserDetails(repoUser);
        InternalDaoAuthenticationProviderTest test = Mockito.spy(InternalDaoAuthenticationProviderTest.class);
        test.setUserDetailsService(userDetailsService);
        test.setSystemLoggedInUserStorage(systemLoggedInUserStorage);
        test.setLoggingService(logEventService);
        Mockito.lenient().doNothing().when((UserAuthorityService)userDetailsService).putUser(Mockito.any(),Mockito.any());
        Mockito.lenient().doNothing().when(logEventService).createUserAccountLockedEvent(metadataUserDetails);
        test.additionalAuthenticationChecks(metadataUserDetails,usernamePasswordAuthenticationToken);
        HttpSession httpSession = mockHttpServletRequest.getSession();
        Assert.assertNotNull(httpSession.getAttribute("numberOfLoginAttemptsRemaining"));
        Assert.assertEquals(Integer.valueOf(1),repoUser.getNumberOfFailedLoginAttempts());
    }

    @Test(expected = BadCredentialsException.class)
    public void testExternallyDefinedUser(){
        RepoUser repoUser = getRepoUser("testUser","testUser");
        MetadataUserDetails metadataUserDetails = new MetadataUserDetails(repoUser);
        metadataUserDetails.setExternallyDefined(true);
        InternalDaoAuthenticationProviderTest test = Mockito.spy(InternalDaoAuthenticationProviderTest.class);
        test.setUserDetailsService(userDetailsService);
        test.setSystemLoggedInUserStorage(systemLoggedInUserStorage);
        //Mockito.doReturn(null).when(systemLoggedInUserStorage).loadUserByNameAndPassword("testUser","testUser");
        test.setExternalUserLoginEventService(Mockito.mock(ExternalUserLoginEventServiceImpl.class));
        setExternalUserLoginEventService(Mockito.mock(ExternalUserLoginEventServiceImpl.class));
        usernamePasswordAuthenticationToken.setDetails(getTenantQualified());
        test.additionalAuthenticationChecks(metadataUserDetails,usernamePasswordAuthenticationToken);
    }

    // JS-67810: Testing user credentials without httpSession available to check past failed attempts
    @Test
    public void testValidateUserForJMXConnectionWithoutHttpSessionAvailable() {
        RequestContextHolder.setRequestAttributes( null );
        RepoUser repoUser = getRepoUser( JOE_USER_USERNAME, JOE_USER_PASSWORD );
        MetadataUserDetails metadataUserDetails = new MetadataUserDetails( repoUser );
        InternalDaoAuthenticationProviderTest test = Mockito.spy( InternalDaoAuthenticationProviderTest.class );
        test.setUserDetailsService( userDetailsService );
        test.setSystemLoggedInUserStorage( systemLoggedInUserStorage );
        test.setLoggingService( logEventService );
        Mockito.lenient().doNothing().when( (UserAuthorityService) userDetailsService ).putUser( Mockito.any(), Mockito.any() );
        Mockito.lenient().doNothing().when( logEventService ).createUserAccountLockedEvent( metadataUserDetails );
        test.additionalAuthenticationChecks( metadataUserDetails, usernamePasswordAuthenticationToken );

        // No exception thrown so far, checking default value
        Assert.assertEquals( 0, repoUser.getNumberOfFailedLoginAttempts().intValue() );
    }


    private RepoUser getRepoUser(String username, String password) {
        RepoUser repoUser = new RepoUser();
        repoUser.setUsername(username);
        repoUser.setPassword(password);
        repoUser.setNumberOfFailedLoginAttempts(0);
        repoUser.setEnabled(true);
        repoUser.setExternallyDefined(false);
        return repoUser;
    }

    private static String getCredentials() {
        return passwordEncoder.encode(InternalDaoAuthenticationProviderTest.JOE_USER_PASSWORD);
    }

    private Principal getPrincipalInstanceForTestUser() {
        Principal principal = new Principal() {
            @Override
            public String getName() {
                return InternalDaoAuthenticationProviderTest.JOE_USER_USERNAME;
            }

        };
        return principal;
    }

    private TenantQualified getTenantQualified() {
        return new TenantQualified() {
            @Override
            public String getTenantId() {
                return "organizations";
            }

            @Override
            public void setTenantId(String tenantId) {
            }
        };
    }
    private static PasswordEncoder getPasswordEncoderfortest()
    {
        String idForEncode = "bcrypt";
        Map encoders = new HashMap<>();
        encoders.put(idForEncode, new BCryptPasswordEncoder());
        PasswordEncoder passwordEncoder =
                new DelegatingPasswordEncoder(idForEncode, encoders);
        return passwordEncoder;
    }

}
