/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.impl;

import static org.junit.Assert.*;

import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.AuthnRequest;

import com.sios.idp.shibboleth.authn.context.UsernameTotpContext;

//import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Expectations;
import net.shibboleth.idp.authn.context.AuthenticationContext;

/**
 * @author SIOS Technology, Inc.
 */
public class ExtractUsernameTotpActionTest {


    @Mocked
    private HttpServletRequest mockRequest;

    /**
     * void doExecute() のテストメソッドです. HttpRequestがnullの場合に、NoCredentialsイベントが登録されているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteHttpRequestNull() throws GeneralSecurityException {
        ExtractUsernameTotpAction action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();

        mockRequest = null;
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        String expected = "NoCredentials";
        String actual = (String) profileRequestContext.getSubcontext(EventContext.class, true).getEvent();

        assertEquals(expected, actual);

    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(usernameFieldName)がnullだった場合に、
     * NoCredentialsイベントが登録されているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteUsernameNull() throws GeneralSecurityException {
        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = null;
                /** 
                mockRequest.getParameter("totp");
                result = "totp";
                mockRequest.getParameter("donotcache");
                result = null;
                mockRequest.getParameter("truststotpauthnsession");
                result = null;*/
            }
        };

        ExtractUsernameTotpAction action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();

        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        String expected = "NoCredentials";
        String actual = (String) profileRequestContext.getSubcontext(EventContext.class, true).getEvent();

        assertEquals(expected, actual);

    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(usernameFieldName)が空文字だった場合に、
     * NoCredentialsイベントが登録されているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteUsernameNullString() throws GeneralSecurityException {

        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = "";
                /**
                mockRequest.getParameter("totp");
                result = "totp";
                mockRequest.getParameter("donotcache");
                result = null;
                mockRequest.getParameter("truststotpauthnsession");
                result = null;*/
            }
        };

        ExtractUsernameTotpAction action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();

        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        String expected = "NoCredentials";
        String actual = (String) profileRequestContext.getSubcontext(EventContext.class, true).getEvent();

        assertEquals(expected, actual);

    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(totpFieldName)がnullだった場合に、
     * NoCredentialsイベントが登録されているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteTotpNull() throws GeneralSecurityException {
        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = "username";
                mockRequest.getParameter("totp");
                result = null;
                /**
                mockRequest.getParameter("donotcache");
                result = null;
                mockRequest.getParameter("truststotpauthnsession");
                result = null;*/
            }
        };

        ExtractUsernameTotpAction action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        String expected = "NoCredentials";
        String actual = (String) profileRequestContext.getSubcontext(EventContext.class, true).getEvent();

        assertEquals(expected, actual);

    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(ssoBypassFieldName)が"1"だった場合に、
     * 認証結果をキャッシュしない(false)になっているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteDonotcacheIs1() throws GeneralSecurityException {

        ExtractUsernameTotpAction action;

        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = "username";
                mockRequest.getParameter("totp");
                result = "totp";
                mockRequest.getParameter("donotcache");
                result = "1";
                mockRequest.getParameter("truststotpauthnsession");
                result = null;
            }
        };

        action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        assertFalse(authenticationContext.isResultCacheable());

    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(ssoBypassFieldName)がNullだった場合に、
     * 認証結果をキャッシュする(true)になっているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteDonotcacheIsNull() throws GeneralSecurityException {

        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = "username";
                mockRequest.getParameter("totp");
                result = "totp";
                mockRequest.getParameter("donotcache");
                result = null;
                mockRequest.getParameter("truststotpauthnsession");
                result = null;
            }
        };

        ExtractUsernameTotpAction action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        assertTrue(authenticationContext.isResultCacheable());

    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(ssoBypassFieldName)が1以外の値が入っていた場合に、
     * 認証結果をキャッシュする(true)になっているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteDonotcache0() throws GeneralSecurityException {
        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = "username";
                mockRequest.getParameter("totp");
                result = "totp";
                mockRequest.getParameter("donotcache");
                result = "0";
                mockRequest.getParameter("truststotpauthnsession");
                result = null;
            }
        };

        ExtractUsernameTotpAction action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        assertTrue(authenticationContext.isResultCacheable());
    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(trustsTotpAuthnSessionFieldName)が"1"だった場合に、
     * TOTP認証セッションを信頼する(true)になっているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteTrustsTotpAuthnSessionIs1() throws GeneralSecurityException {

        ExtractUsernameTotpAction action;

        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = "username";
                mockRequest.getParameter("totp");
                result = "totp";
                mockRequest.getParameter("donotcache");
                result = null;
                mockRequest.getParameter("truststotpauthnsession");
                result = "1";
            }
        };

        action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        final UsernameTotpContext usernameTotpContext = authenticationContext.getSubcontext(
                UsernameTotpContext.class, true);
        assertTrue(usernameTotpContext.trustsTotpAuthnSession());
    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(trustsTotpAuthnSessionFieldName)がNullだった場合に、
     * TOTP認証セッションを信頼しない(false)になっているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteTrustsTotpAuthnSessionIsNull() throws GeneralSecurityException {

        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = "username";
                mockRequest.getParameter("totp");
                result = "totp";
                mockRequest.getParameter("donotcache");
                result = null;
                mockRequest.getParameter("truststotpauthnsession");
                result = null;
            }
        };

        ExtractUsernameTotpAction action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        final UsernameTotpContext usernameTotpContext = authenticationContext.getSubcontext(
                UsernameTotpContext.class, true);
        assertFalse(usernameTotpContext.trustsTotpAuthnSession());
    }

    /**
     * void doExecute() のテストメソッドです. request.getParameter(trustsTotpAuthnSessionFieldName)が1以外の値が入っていた場合に、
     * TOTP認証セッションを信頼しない(false)になっているかテストします。
     * @throws GeneralSecurityException セキュリティー関連
     */
    @Test
    public void testDoExecuteTrustsTotpAuthnSessionIs0() throws GeneralSecurityException {
        new Expectations() {
            {
                mockRequest.getParameter("username");
                result = "username";
                mockRequest.getParameter("totp");
                result = "totp";
                mockRequest.getParameter("donotcache");
                result = null;
                mockRequest.getParameter("truststotpauthnsession");
                result = "0";
            }
        };

        ExtractUsernameTotpAction action = new ExtractUsernameTotpAction();
        ProfileRequestContext<AuthnRequest, Object> profileRequestContext
            = new ProfileRequestContext<AuthnRequest, Object>();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        //Deencapsulation.setField(action, "httpServletRequest", mockRequest);
        action.setHttpServletRequest(mockRequest);
        action.doExecute(profileRequestContext, authenticationContext);

        final UsernameTotpContext usernameTotpContext = authenticationContext.getSubcontext(
                UsernameTotpContext.class, true);
        assertFalse(usernameTotpContext.trustsTotpAuthnSession());
    }
}
