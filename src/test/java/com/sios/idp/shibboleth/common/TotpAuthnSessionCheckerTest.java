/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common;

import static org.junit.Assert.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import com.sios.idp.shibboleth.common.util.TotpAuthnSessionIdGenerator;
import com.sios.idp.shibboleth.exception.TotpAuthnSessionIdGenerationException;

import mockit.Mocked;
import mockit.Expectations;

/**
 * {@link com.sios.idp.shibboleth.common.TotpAuthnSessionChecker}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpAuthnSessionCheckerTest {

    /**
     * テスト対象クラスです.
     */
    private TotpAuthnSessionChecker totpAuthnSessionChecker;

    @Mocked
    private HttpServletRequest mockRequest;

    @Mocked
    private AppConfig appConfig;

    @Mocked
    private TotpAuthnSessionIdGenerator totpAuthnSessionIdGenerator;

    /**
     * テスト対象クラスの全テストで使用する、共通のインスタンスを生成し初期設定を行います.
     */
    @Before
    public void setUp() {
        totpAuthnSessionChecker = new TotpAuthnSessionChecker();
        totpAuthnSessionChecker.setHttpServletRequest(mockRequest);
    }

    /**
     * ユーザー名から計算したセッションIDと、Cookieに保存されたセッションIDが同じ場合、
     * 正当なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateWithValidSession() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                AppConfig.getTotpAuthnSessionIssueDateCookieName();
                result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 2592000;
                TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                result = "SESSIONID";
                Cookie sessionId = new Cookie("totp-session-id", "SESSIONID");
                Cookie sessionTime = new Cookie("totp-session-time", "9999999999");
                mockRequest.getCookies();
                result = new Cookie[] { sessionId, sessionTime };
            }
        };
        assertTrue(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

    /**
     * ユーザー名から計算したセッションIDと、Cookieに保存されたセッションIDが異なる場合、
     * 不正なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateWithInvalidSessionId() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                AppConfig.getTotpAuthnSessionIssueDateCookieName();
                result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 2592000;
                TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                result = "INVALID";
                Cookie sessionId = new Cookie("totp-session-id", "SESSIONID");
                Cookie sessionTime = new Cookie("totp-session-time", "9999999999");
                mockRequest.getCookies();
                result = new Cookie[] { sessionId, sessionTime };
            }
        };
        assertFalse(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

    /**
     * Cookieが保存されていない場合、
     * 不正なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateWithoutCookie() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                //AppConfig.getTotpAuthnSessionIdCookieName();
                //result = "totp-session-id";
                //AppConfig.getTotpAuthnSessionIssueDateCookieName();
                //result = "totp-session-time";
                //AppConfig.getTotpAuthnSessionExpirationSec();
                //result = 2592000;
                //TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                //result = "SESSIONID";
                mockRequest.getCookies();
                result = null;
            }
        };
        assertFalse(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

    /**
     * Cookieにセッションが保存されていない場合、
     * 不正なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateWithoutSession() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                //AppConfig.getTotpAuthnSessionIdCookieName();
                //result = "totp-session-id";
                //AppConfig.getTotpAuthnSessionIssueDateCookieName();
                //result = "totp-session-time";
                //AppConfig.getTotpAuthnSessionExpirationSec();
                //result = 2592000;
                //TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                //result = "SESSIONID";
                mockRequest.getCookies();
                result = new Cookie[] {  };
            }
        };
        assertFalse(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

    /**
     * CookieにセッションIDが保存されていない場合、
     * 不正なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateWithoutSessionId() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                AppConfig.getTotpAuthnSessionIssueDateCookieName();
                result = "totp-session-time";
                //AppConfig.getTotpAuthnSessionExpirationSec();
                //result = 2592000;
                //TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                //result = "SESSIONID";
                Cookie sessionTime = new Cookie("totp-session-time", "9999999999");
                mockRequest.getCookies();
                result = new Cookie[] { sessionTime };
            }
        };
        assertFalse(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

    /**
     * Cookieにセッション処理日時が保存されていない場合、
     * 不正なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateWithoutIssueDate() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                //AppConfig.getTotpAuthnSessionIssueDateCookieName();
                //result = "totp-session-time";
                //AppConfig.getTotpAuthnSessionExpirationSec();
                //result = 2592000;
                //TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                //result = "SESSIONID";
                Cookie sessionId = new Cookie("totp-session-id", "SESSIONID");
                mockRequest.getCookies();
                result = new Cookie[] { sessionId };
            }
        };
        assertFalse(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

    /**
     * Cookieに保存されているセッション処理日時が数値に解釈できない場合、
     * 不正なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateWithInvalidIssueDate() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                AppConfig.getTotpAuthnSessionIssueDateCookieName();
                result = "totp-session-time";
                //AppConfig.getTotpAuthnSessionExpirationSec();
                //result = 2592000;
                //TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                //result = "SESSIONID";
                Cookie sessionId = new Cookie("totp-session-id", "SESSIONID");
                Cookie sessionTime = new Cookie("totp-session-time", "INVALID_SESSION_TIME");
                mockRequest.getCookies();
                result = new Cookie[] { sessionId, sessionTime };
            }
        };
        assertFalse(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

    /**
     * Cookieに保存されているセッション処理日時が有効期限切れの場合、
     * 不正なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateWithExpiredIssueDate() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                AppConfig.getTotpAuthnSessionIssueDateCookieName();
                result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 2592000;
                //TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                //result = "SESSIONID";
                Cookie sessionId = new Cookie("totp-session-id", "SESSIONID");
                Cookie sessionTime = new Cookie("totp-session-time", "0");
                mockRequest.getCookies();
                result = new Cookie[] { sessionId, sessionTime };
            }
        };
        assertFalse(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

    /**
     * 検証用セッションIDの計算でエラーが発生した場合、
     * 不正なセッションと判定されることをテストします.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成時の例外
     */
    @Test
    public void validateTotpAuthnSessionIdGenerationError() throws TotpAuthnSessionIdGenerationException {
        //new Expectations(AppConfig.class, TotpAuthnSessionIdGenerator.class) {
        new Expectations() {
            {
                AppConfig.getTotpAuthnSessionIdCookieName();
                result = "totp-session-id";
                AppConfig.getTotpAuthnSessionIssueDateCookieName();
                result = "totp-session-time";
                AppConfig.getTotpAuthnSessionExpirationSec();
                result = 2592000;
                TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(anyString, anyString);
                result = new TotpAuthnSessionIdGenerationException();
                Cookie sessionId = new Cookie("totp-session-id", "SESSIONID");
                Cookie sessionTime = new Cookie("totp-session-time", "9999999999");
                mockRequest.getCookies();
                result = new Cookie[] { sessionId, sessionTime };
            }
        };
        assertFalse(totpAuthnSessionChecker.isValidTotpAuthnSession("username"));
    }

}
