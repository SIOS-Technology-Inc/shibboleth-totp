/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.AuthnRequest;

//import com.sios.idp.shibboleth.authn.context.UsernameTotpContext;
import com.sios.idp.shibboleth.authn.jaas.OneTimePasswordCallback;
import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.datasource.dao.Dao;
import com.sios.idp.shibboleth.datasource.dao.DaoFactory;
import com.sios.idp.shibboleth.dto.SearchResult;
import com.sios.idp.shibboleth.exception.DaoInstantiationException;
import com.sios.idp.shibboleth.exception.DataAccessException;
import com.sios.idp.shibboleth.exception.TotpAuthnSessionIdGenerationException;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;

//import mockit.Deencapsulation;
import mockit.Delegate;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.Expectations;
import mockit.Injectable;
import net.shibboleth.idp.authn.AbstractValidationAction;
import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
//import static mockit.Deencapsulation.*;

/**
 * @author SIOS Technology, Inc.
 */
public class ValidateUsernameTotpActionTest {
    @Tested
    ValidateUsernameTotpAction action;

    //private static final String IMMUTABLE_USER_ID_ATTR_NAME = "immutableUserIdAttr";

    @Mocked
    private HttpServletRequest mockRequest;
    @Mocked
    private HttpServletResponse mockResponse;
    @Mocked
    Configuration mockConfiguration;
    @Mocked
    javax.security.auth.login.LoginContext mockJaasLoginCtx;
    @Mocked
    DaoFactory _daoFactory;
    @Mocked
    Dao _dao;
    @Mocked
    final AppConfig _appConfig = null;
    @Injectable
    private com.sios.idp.shibboleth.authn.context.UsernameTotpContext utContext;

    /**
     * void doExecute() のテストメソッドです. TOTP認証かつログイン成功の場合に正常終了すること、
     * authenticationContextとprofileRequestContextには認証結果のサブジェクトが登録されていることを をテスト
     * 
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteTotpNormal() throws GeneralSecurityException {
        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                mockJaasLoginCtx.login();
                ;
                utContext.getUsername();
                result = "username";
            }
        };

        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(false);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("testFlow");
        ArrayList<Principal> principals = new ArrayList<Principal>();
        // 設定しないとDefaultPrincipalが有効になる
        flow.setSupportedPrincipals(principals);
        authenticationContext.setAttemptedFlow(flow);

        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        try {
            action.doExecute(profileRequestContext, authenticationContext);

            javax.security.auth.Subject sub = authenticationContext.getAuthenticationResult().getSubject();
            assertNotNull(sub);
            boolean subjectCompareflg = false;
            for (Principal prin : sub.getPrincipals()) {
                if (prin.getName().equals("username")) {
                    subjectCompareflg = true;
                }
            }
            assertTrue(subjectCompareflg);

            final SubjectCanonicalizationContext c14n = new SubjectCanonicalizationContext();
            SubjectCanonicalizationContext subjectCanonicalizationContext = profileRequestContext
                    .getSubcontext(c14n.getClass());
            javax.security.auth.Subject pSub = subjectCanonicalizationContext.getSubject();

            boolean pSubCompFlg = false;
            for (Principal pSubPrin : pSub.getPrincipals()) {
                if (pSubPrin.getName().equals("username")) {
                    pSubCompFlg = true;
                }
            }

            assertTrue(pSubCompFlg);

        } catch (Exception e) {
            fail("例外が発生したのでテスト不合格");
        }

    }

    /**
     * void doExecute() のテストメソッドです. TOTP認証かつログインが失敗(LoginException)した場合に
     * AuthenticationExceptionイベントが登録されているかテストします。
     * 
     * @throws GeneralSecurityException  セキュリティー関連
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @Test
    public void testDoExecuteTotpLoginException() throws GeneralSecurityException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        new MockUp<AbstractValidationAction>() {
            @Mock
            protected void buildAuthenticationResult(@Nonnull final ProfileRequestContext profileRequestContext,
                    @Nonnull final AuthenticationContext authenticationContext) {
                ;
            }
        };

        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                mockJaasLoginCtx.login();
                ;
                result = new LoginException("ログイン失敗");
                utContext.getUsername();
                result = "username";
            }
        };

        /** 
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(false);**/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        /**
        Method method = ValidateUsernameTotpAction.class.getDeclaredMethod("setHttpServletRequest", HttpServletRequest.class);
        method.setAccessible(true);

        method.invoke(action, mockRequest);**/




        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        try {
            action.doExecute(profileRequestContext, authenticationContext);
            assertEquals("AuthenticationException",
                    (String) profileRequestContext.getSubcontext(EventContext.class, true).getEvent());
        } catch (Exception e) {
            fail("例外がスローされたので不合格");
        }

    }

    /**
     * void doExecute() のテストメソッドです. TOTP認証かつ回復不能例外(NoSuchAlgorithmException)の場合に、
     * Exceptionがスローされるかをテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteTotpNoRecoverableException() throws GeneralSecurityException {

        new MockUp<AbstractValidationAction>() {
            @Mock
            protected void buildAuthenticationResult(@Nonnull final ProfileRequestContext profileRequestContext,
                    @Nonnull final AuthenticationContext authenticationContext) {
                ;
            }
        };
        /* new MockUp<Configuration>(){
         *
         * @Mock public Configuration getInstance()throws NoSuchAlgorithmException{ throw new
         * NoSuchAlgorithmException(); } }; */

        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                //mockJaasLoginCtx.login();
                //;
                Configuration.getInstance(anyString, (Configuration.Parameters) any);
                result = new NoSuchAlgorithmException();
                utContext.getUsername();
                result = "username";
            }
        };

        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(false);**/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");
        

        try {
            action.doExecute(profileRequestContext, authenticationContext);
            fail("回復不能例外で例外がスローされなかったので不合格");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof NoSuchAlgorithmException);
        }

    }


    /**
     * void doExecute() のテストメソッドです. TOTP認証かつログイン成功の場合で、
     * TOTP認証セッション信頼フラグがtrueになっている場合、Cookieに値・有効期限がセットされることをテストします.
     * @throws GeneralSecurityException セキュリティー関連
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッション生成時の例外
     */
    @Test
    public void testDoExecuteWithTrustsTotpAuthnSession() throws GeneralSecurityException, TotpAuthnSessionIdGenerationException, DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final Map<String,Cookie> resultCookies = new HashMap<String,Cookie>();

        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                mockJaasLoginCtx.login();
                ;
                DaoFactory.getInstance();
                result = _daoFactory;
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add("gAuth", "secretKey");
                _dao.getUser(anyString);
                result = searchResult;
                AppConfig.getSecretKeyAttributeName();
                result = "gAuth";
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                AppConfig.getTotpAuthnSessionIssueDateCookieName();
                result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 2592000;
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
                mockRequest.getCookies();
                result = null;
                mockResponse.addCookie((Cookie)any);
                result = new Delegate() {
                    public void delegate(Cookie cookie) {
                    	resultCookies.put(cookie.getName(), cookie);
                        return;
                    }
                };
                utContext.getUsername();
                result = "username";
                utContext.trustsTotpAuthnSession();
                result = true;
            }
        };

        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(true);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("testFlow");
        ArrayList<Principal> principals = new ArrayList<Principal>();
        // 設定しないとDefaultPrincipalが有効になる
        flow.setSupportedPrincipals(principals);
        authenticationContext.setAttemptedFlow(flow);

        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        try {
            action.doExecute(profileRequestContext, authenticationContext);

            assertTrue(resultCookies.containsKey("totp-session-id"));
            assertEquals(2592000, resultCookies.get("totp-session-id").getMaxAge());

            assertTrue(resultCookies.containsKey("totp-session-time"));
            assertEquals(2592000, resultCookies.get("totp-session-time").getMaxAge());
        } catch (Exception e) {
            fail("例外が発生したのでテスト不合格");
        }

    }

    /**
     * void doExecute() のテストメソッドです. TOTP認証かつログイン成功の場合で、
     * TOTP認証セッションIDの生成中にエラーが発生した場合、Cookieがセットされないことをテストします.
     * @throws GeneralSecurityException セキュリティー関連
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッション生成時の例外
     */
    @Test
    public void testDoExecuteTotpAuthnSessionIdGenerationException() throws GeneralSecurityException, TotpAuthnSessionIdGenerationException, DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final Map<String,Cookie> resultCookies = new HashMap<String,Cookie>();

        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                mockJaasLoginCtx.login();
                ;
                DaoFactory.getInstance();
                result = _daoFactory;
                _daoFactory.createInstance();
                result = _dao;
                _dao.getUser(anyString);
                result = null; // ユーザーが見つからない
                //AppConfig.getSecretKeyAttributeName();
                //result = "gAuth";
                //AppConfig.getTotpAuthnSessionIdCookieName();
                //result = "totp-session-id";
                //AppConfig.getTotpAuthnSessionIssueDateCookieName();
                //result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 2592000;
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
                //mockRequest.getCookies();
                //result = null;
                /**mockResponse.addCookie((Cookie)any);
                result = new Delegate() {
                    public void delegate(Cookie cookie) {
                    	resultCookies.put(cookie.getName(), cookie);
                        return;
                    }
                };*/
                utContext.getUsername();
                result = "username";
                utContext.trustsTotpAuthnSession();
                result = true;
            }
        };

        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(true);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("testFlow");
        ArrayList<Principal> principals = new ArrayList<Principal>();
        // 設定しないとDefaultPrincipalが有効になる
        flow.setSupportedPrincipals(principals);
        authenticationContext.setAttemptedFlow(flow);

        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        try {
            action.doExecute(profileRequestContext, authenticationContext);

            assertFalse(resultCookies.containsKey("totp-session-id"));
            assertFalse(resultCookies.containsKey("totp-session-time"));
        } catch (Exception e) {
            fail("例外が発生したのでテスト不合格");
        }

    }

    /**
     * void doExecute() のテストメソッドです. TOTP認証かつログイン成功の場合で、
     * TOTP認証セッション信頼フラグがfalseになっている場合、Cookieがセットされないことをテストします.
     * @throws GeneralSecurityException セキュリティー関連
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッション生成時の例外
     */
    @Test
    public void testDoExecuteWithoutTrustsTotpAuthnSession() throws GeneralSecurityException, TotpAuthnSessionIdGenerationException, DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final Map<String,Cookie> resultCookies = new HashMap<String,Cookie>();

        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                mockJaasLoginCtx.login();
                ;
                //DaoFactory.getInstance();
                //result = _daoFactory;
                //_daoFactory.createInstance();
                //result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add("gAuth", "secretKey");
                //_dao.getUser(anyString);
                //result = searchResult;
                //AppConfig.getSecretKeyAttributeName();
                //result = "gAuth";
                //AppConfig.getTotpAuthnSessionIdCookieName();
                //result = "totp-session-id";
                //AppConfig.getTotpAuthnSessionIssueDateCookieName();
                //result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 2592000;
                //AppConfig.getTotpAuthnSessionIdSalt();
                //result = "Salt";
                //mockRequest.getCookies();
                //result = null;
                /**mockResponse.addCookie((Cookie)any);
                result = new Delegate() {
                    public void delegate(Cookie cookie) {
                    	resultCookies.put(cookie.getName(), cookie);
                        return;
                    }
                };*/
                utContext.getUsername();
                result = "username";
                utContext.trustsTotpAuthnSession();
                result = false;
            }
        };

        /** 
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(false); //ユーザーがTOTP認証セッションを信頼しないを選択*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("testFlow");
        ArrayList<Principal> principals = new ArrayList<Principal>();
        // 設定しないとDefaultPrincipalが有効になる
        flow.setSupportedPrincipals(principals);
        authenticationContext.setAttemptedFlow(flow);

        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        try {
            action.doExecute(profileRequestContext, authenticationContext);

            assertFalse(resultCookies.containsKey("totp-session-id"));
            assertFalse(resultCookies.containsKey("totp-session-time"));
        } catch (Exception e) {
            fail("例外が発生したのでテスト不合格");
        }

    }

    /**
     * void doExecute() のテストメソッドです. TOTP認証かつログイン成功の場合で、
     * TOTP認証セッションの有効期間が0になっている場合、Cookieがセットされないことをテストします.
     * @throws GeneralSecurityException セキュリティー関連
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッション生成時の例外
     */
    @Test
    public void testDoExecuteTrustTotpAuthnSessionIsDisabled() throws GeneralSecurityException, TotpAuthnSessionIdGenerationException, DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final Map<String,Cookie> resultCookies = new HashMap<String,Cookie>();

        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                mockJaasLoginCtx.login();
                ;
                //DaoFactory.getInstance();
                //result = _daoFactory;
                //_daoFactory.createInstance();
                //result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add("gAuth", "secretKey");
                //_dao.getUser(anyString);
                //result = searchResult;
                //AppConfig.getSecretKeyAttributeName();
                //result = "gAuth";
                //AppConfig.getTotpAuthnSessionIdCookieName();
                //result = "totp-session-id";
                //AppConfig.getTotpAuthnSessionIssueDateCookieName();
                //result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 0; // TOTP認証セッション有効期間を0に設定
                //AppConfig.getTotpAuthnSessionIdSalt();
                //result = "Salt";
                //mockRequest.getCookies();
                //result = null;
                /**mockResponse.addCookie((Cookie)any);
                result = new Delegate() {
                    public void delegate(Cookie cookie) {
                    	resultCookies.put(cookie.getName(), cookie);
                        return;
                    }
                };*/
                utContext.getUsername();
                result = "username";
            }
        };

        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(true);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("testFlow");
        ArrayList<Principal> principals = new ArrayList<Principal>();
        // 設定しないとDefaultPrincipalが有効になる
        flow.setSupportedPrincipals(principals);
        authenticationContext.setAttemptedFlow(flow);

        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        try {
            action.doExecute(profileRequestContext, authenticationContext);

            assertFalse(resultCookies.containsKey("totp-session-id"));
            assertFalse(resultCookies.containsKey("totp-session-time"));
        } catch (Exception e) {
            fail("例外が発生したのでテスト不合格");
        }

    }

    /**
     * void doExecute() のテストメソッドです. TOTP認証セッション信頼フラグがtrueになっていて、
     * 既に別のセッションがCookie保存済の場合、それが削除されることをテストします.
     * @throws GeneralSecurityException セキュリティー関連
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッション生成時の例外
     */
    @Test
    public void testDoExecuteTotpAuthnSessionAlreadyExists() throws GeneralSecurityException, TotpAuthnSessionIdGenerationException, DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final Cookie sessionId = new Cookie("totp-session-id", "SESSIONID");
        sessionId.setMaxAge(2592000);
        final Cookie sessionTime = new Cookie("totp-session-time", "9999999999");
        sessionTime.setMaxAge(2592000);

        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                mockJaasLoginCtx.login();
                ;
                DaoFactory.getInstance();
                result = _daoFactory;
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add("gAuth", "secretKey");
                _dao.getUser(anyString);
                result = searchResult;
                AppConfig.getSecretKeyAttributeName();
                result = "gAuth";
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                AppConfig.getTotpAuthnSessionIssueDateCookieName();
                result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 2592000;
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
                mockRequest.getCookies();
                result = new Cookie[] { sessionId, sessionTime };
                mockResponse.addCookie((Cookie)any);
                ;
                utContext.getUsername();
                result = "username";
                utContext.trustsTotpAuthnSession();
                result = true;
            }
        };

        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(true);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        AuthenticationFlowDescriptor flow = new AuthenticationFlowDescriptor();
        flow.setId("testFlow");
        ArrayList<Principal> principals = new ArrayList<Principal>();
        // 設定しないとDefaultPrincipalが有効になる
        flow.setSupportedPrincipals(principals);
        authenticationContext.setAttemptedFlow(flow);

        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        try {
            action.doExecute(profileRequestContext, authenticationContext);
            assertEquals(0, sessionId.getMaxAge());
            assertEquals(0, sessionTime.getMaxAge());
        } catch (Exception e) {
            fail("例外が発生したのでテスト不合格");
        }

    }

    /**
     * void doPreExecute() のテストメソッドです. TOTP認証の場合に正常終了することをテスト
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void testDoPreExecuteTotpNormal() throws DaoInstantiationException, UserDuplicatedException,
            DataAccessException, UnexpectedException {
        new MockUp<AbstractValidationAction>() {
            @Mock
            protected boolean doPreExecute(ProfileRequestContext profileRequestContext,
                    AuthenticationContext authenticationContext) {
                return true;
            }
        };

        new Expectations() {
            {
                //mockRequest.getRemoteAddr();
                //result = "10.10.10.10";
                //AppConfig.getImmutableUserIdAttributeName();
                //result = IMMUTABLE_USER_ID_ATTR_NAME;
                utContext.getUsername();
                result = "username";
                utContext.getTotp();
                result = "123456";
            }
        };
        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(false);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        // サブコンテキストを事前に登録
        authenticationContext.addSubcontext(utContext, true);
        try {
            assertTrue(action.doPreExecute(profileRequestContext, authenticationContext));
        } catch (Exception e) {
            fail("例外が発生したので不合格");
        }
    }

    /**
     * void doPreExecute() のテストメソッドです. TOTP認証かつContextが取得できない場合に、doPreExecute()がfalseとなり、
     * ProfileRequestContext に NoCredentialsイベントが設定されること をテストします。
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void testDoPreExecuteTotpContextNull() throws DaoInstantiationException, UserDuplicatedException,
            DataAccessException, UnexpectedException {

        new MockUp<AbstractValidationAction>() {
            @Mock
            protected boolean doPreExecute(ProfileRequestContext profileRequestContext,
                    AuthenticationContext authenticationContext) {
                return true;
            }
        };
        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                //AppConfig.getImmutableUserIdAttributeName();
                //result = IMMUTABLE_USER_ID_ATTR_NAME;
            }
        };
        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(false);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        // サブコンテキストを事前に登録しないため、サブコンテキストの取得でエラーになる
        try {
            assertFalse(action.doPreExecute(profileRequestContext, authenticationContext));
            assertEquals("NoCredentials", (String) profileRequestContext.getSubcontext(EventContext.class, true)
                    .getEvent());
        } catch (Exception e) {
            fail("例外が発生したので不合格");
        }
    }

    /**
     * void doPreExecute() のテストメソッドです. TOTP認証かつユーザ名が取得できない場合に、doPreExecute()がfalseとなり、
     * ProfileRequestContext に NoCredentialsイベントが設定されること をテストします。
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void testDoPreExecuteTotpNameNull() throws DaoInstantiationException, UserDuplicatedException,
            DataAccessException, UnexpectedException {
        new MockUp<AbstractValidationAction>() {
            @Mock
            protected boolean doPreExecute(ProfileRequestContext profileRequestContext,
                    AuthenticationContext authenticationContext) {
                return true;
            }
        };

        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                //AppConfig.getImmutableUserIdAttributeName();
                //result = IMMUTABLE_USER_ID_ATTR_NAME;
                utContext.getUsername();
                result = null;
            }
        };
        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername(null); // ユーザ名が取得できない
        context.setTotp("123456");
        context.setTrustsTotpAuthnSession(false);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        // サブコンテキストを事前に登録
        authenticationContext.addSubcontext(utContext, true);
        try {
            assertFalse(action.doPreExecute(profileRequestContext, authenticationContext));
            assertEquals("NoCredentials", (String) profileRequestContext.getSubcontext(EventContext.class, true)
                    .getEvent());
        } catch (Exception e) {
            fail("例外が発生したので不合格");
        }
    }

    /**
     * void doPreExecute() のテストメソッドです. TOTP認証かつTOTPが取得できない場合に、doPreExecute()がfalseとなり、
     * ProfileRequestContext に NoCredentialsイベントが設定されること をテストします。
     * @throws DaoInstantiationException Daoクラスの初期化
     * @throws UserDuplicatedException ユーザが複数存在
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void testDoPreExecuteTotpTotpNull() throws DaoInstantiationException, UserDuplicatedException,
            DataAccessException, UnexpectedException {
        new MockUp<AbstractValidationAction>() {
            @Mock
            protected boolean doPreExecute(ProfileRequestContext profileRequestContext,
                    AuthenticationContext authenticationContext) {
                return true;
            }
        };
        new Expectations() {
            {
                mockRequest.getRemoteAddr();
                result = "10.10.10.10";
                //AppConfig.getImmutableUserIdAttributeName();
                //result = IMMUTABLE_USER_ID_ATTR_NAME;
                utContext.getUsername();
                result = "username";
                utContext.getTotp();
                result = null;
            }
        };
        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername("username");
        context.setTotp(null); // ワンタイムトークンが取得できない
        context.setTrustsTotpAuthnSession(false);*/

        //ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        //Deencapsulation.setField(action, "httpServletResponse", mockResponse);
        //Deencapsulation.setField(action, "utContext", context);
        //Deencapsulation.setField(action, "totpJaasLoginConfigName", "TOTP");
        action.setHttpServletRequest(mockRequest);
        action.setHttpServletResponse(mockResponse);
        action.setTotpJaasLoginConfigName("TOTP");

        // サブコンテキストを事前に登録
        authenticationContext.addSubcontext(utContext, true);
        try {
            assertFalse(action.doPreExecute(profileRequestContext, authenticationContext));
            assertEquals("NoCredentials", (String) profileRequestContext.getSubcontext(EventContext.class, true)
                    .getEvent());
        } catch (Exception e) {
            fail("例外が発生したので不合格");
        }
    }

    /**
     * void handle(Callback[] callbacks) のテストメソッドです. 引数のCallback配列に値が設定されていることをテストします。
     * @throws UnsupportedCallbackException コールバックハンドラがサポートしていない呼び出し
     * @throws IOException 入出力関係
     */
    @Test
    public void testHandlerNormal() throws IOException, UnsupportedCallbackException {

        final String expUserName = "userName";
        final String expTotp = "oneTimePassword";

        new Expectations() {
            {
                utContext.getUsername();
                result = expUserName;
                utContext.getTotp();
                result = expTotp;
            }
        };

        Callback[] callbacks = new Callback[] { new OneTimePasswordCallback("totp"),
                new NameCallback("name"), };
        /**
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername(expUserName);
        context.setTotp(expTotp);
        context.setTrustsTotpAuthnSession(false);
        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        Deencapsulation.setField(action, "utContext", context);*/

        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler handler
             = action.new UsernameTotpAuthnCallbackHandler();
        handler.handle(callbacks);

        assertEquals(expTotp, ((OneTimePasswordCallback) callbacks[0]).getOneTimePassword());
        assertEquals(expUserName, ((NameCallback) callbacks[1]).getName());
    }

    /**
     * void handle(Callback[] callbacks) のテストメソッドです.
     * 引数のCallback配列にnullを渡した場合、例外が発生せず、Callback配列も変更されないことをテストします。
     * @throws UnsupportedCallbackException コールバックハンドラがサポートしていない呼び出し
     * @throws IOException 入出力関係
     */
    @Test
    public void testHandlerNull() throws IOException, UnsupportedCallbackException {

        /**
        final String expUserName = "userName";
        final String expTotp = "oneTimePassword";

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername(expUserName);
        context.setTotp(expTotp);
        context.setTrustsTotpAuthnSession(false);
        Deencapsulation.setField(action, "utContext", context);*/

        Callback[] callbacks = null;
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler handler
            = action.new UsernameTotpAuthnCallbackHandler();
        try {
            handler.handle(callbacks);
            assertNull(callbacks);
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * void handle(Callback[] callbacks) のテストメソッドです.
     * 引数のCallback配列に未サポートのCallbackを渡した場合、そのCallbackに値が設定されないことをテストします。
     * サポートしているCallbackについては正常に値が取得できることをテストします。
     * @throws UnsupportedCallbackException コールバックハンドラがサポートしていない呼び出し
     * @throws IOException 入出力関係
     */
    @Test
    public void testHandlerNotSupport() throws IOException, UnsupportedCallbackException {

        final String expUserName = "userName";
        final String expTotp = "oneTimePassword";

        new Expectations() {
            {
                utContext.getUsername();
                result = expUserName;
                utContext.getTotp();
                result = expTotp;
            }
        };

        Callback[] callbacks = new Callback[] { new OneTimePasswordCallback("totp"),
                new NameCallback("name"), new TextInputCallback("textInput"), };

        /**
        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        UsernameTotpContext context = new UsernameTotpContext();
        context.setUsername(expUserName);
        context.setTotp(expTotp);
        context.setTrustsTotpAuthnSession(false);
        Deencapsulation.setField(action, "utContext", context);*/

        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler handler
           = action.new UsernameTotpAuthnCallbackHandler();
        handler.handle(callbacks);

        assertEquals(expTotp, ((OneTimePasswordCallback) callbacks[0]).getOneTimePassword());
        assertEquals(expUserName, ((NameCallback) callbacks[1]).getName());
        assertNull(((TextInputCallback) callbacks[2]).getText());
    }
}
