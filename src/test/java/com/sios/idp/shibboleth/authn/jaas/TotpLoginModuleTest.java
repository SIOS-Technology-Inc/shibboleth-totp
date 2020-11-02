/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
//CHECKSTYLE:OFF テストクラスのため
package com.sios.idp.shibboleth.authn.jaas;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Expectations;

import org.junit.Test;

import com.sios.idp.shibboleth.authn.impl.ValidateUsernameTotpAction;
import com.sios.idp.shibboleth.authn.impl.ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler;
import com.sios.idp.shibboleth.authn.totp.Totp;
import com.sios.idp.shibboleth.authn.totp.TotpCache;
import com.sios.idp.shibboleth.authn.totp.TotpProvider;
import com.sios.idp.shibboleth.authn.totp.TotpProviderImpl;
import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.exception.TotpGenerationException;
import com.sun.security.auth.UserPrincipal;

/**
 * {@link com.sios.idp.shibboleth.authn.jaas.TotpLoginModule}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpLoginModuleTest {

    @Mocked
    final AppConfig _appConfig = null;

    /**
     * 001: void initialize() のテストメソッドです. login.confのオプションが正しく取得できること、
     * 生成されたTotpProviderのインスタンスがlogin.confで指定したクラスであることをテストします。
     */
    @Test
    public void testInitialize001() {

        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        new MockUp<UsernameTotpAuthnCallbackHandler>() {};
        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(subject, callbackHandler, sharedState, options);

        Object totpProvider = null;
        try {
            Field totpProviderField = loginModule.getClass().getDeclaredField("totpProvider");
            totpProviderField.setAccessible(true);
            totpProvider = totpProviderField.get(loginModule);
        } catch (Exception e) {
            fail("例外は発生しないはず");
        }
        assertEquals("com.sios.idp.shibboleth.authn.totp.TotpProviderImpl", totpProvider.getClass().getName());
    }

    /**
     * 002: void initialize() のテストメソッドです. login.conf の「TotpProviderClass」に存在しない不正なクラスが指定されている場合、
     * 期待されるメッセージを保持したRuntimeExceptionがthrowされることをテストします。
     */
    @Test
    public void testInitialize002() {

        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        String invalidClassName = "InvalidTotpProviderImpl";
        options.put("TotpProviderClass", invalidClassName);

        new MockUp<UsernameTotpAuthnCallbackHandler>() {};
        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();

        TotpLoginModule loginModule = new TotpLoginModule();
        try {
            loginModule.initialize(subject, callbackHandler, sharedState, options);
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
            assertEquals(MessageFormat.format("{0}: TOTPプロバイダ「{1}」のインスタンス生成に失敗しました。",
                    "com.sios.idp.shibboleth.exception.TotpProviderInstantiationException", invalidClassName),
                    e.getMessage());
        }
    }

    /**
     * 003: void initialize() のテストメソッドです. login.conf
     * の「TotpProviderClass」に「TotpProvider」を実装していないクラスが指定されている場合、
     * 期待されるメッセージを保持したRuntimeExceptionがthrowされることをテストします。
     */
    @Test
    public void testInitialize003() {

        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        String invalidClassName = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
        options.put("TotpProviderClass", invalidClassName);

        new MockUp<UsernameTotpAuthnCallbackHandler>() {};
        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();

        TotpLoginModule loginModule = new TotpLoginModule();
        try {
            loginModule.initialize(subject, callbackHandler, sharedState, options);
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
            assertEquals(MessageFormat.format("{0}: JAASログイン構成ファイルのプロパティ「TotpProviderClass：{1}」が {2} を実装していません。",
                    "com.sios.idp.shibboleth.exception.TotpProviderInstantiationException", invalidClassName,
                    TotpProvider.class.getName()), e.getMessage());
        }
    }

    /**
     * 004: void initialize() のテストメソッドです. login.conf
     * の「TotpProviderClass」に「TotpProvider」を実装していないクラスが指定されている場合、
     * 期待されるメッセージを保持したRuntimeExceptionがthrowされることをテストします。
     */
    @Test(expected = RuntimeException.class)
    public void testInitialize004() {

        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", null);

        new MockUp<UsernameTotpAuthnCallbackHandler>() {};
        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(subject, callbackHandler, sharedState, options);
        fail("例外が発生するはず");

    }

    /**
     * 005: void login() のテストメソッドです. CallbackHandlerがIOExceptionをthrowした場合
     * RuntimeExceptionがthrowされることをテストします。
     * @throws LoginException 認証エラーの場合
     */
    @Test(expected = RuntimeException.class)
    public void testLogin001() throws LoginException {

        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                throw new IOException();
            }
        };
        new MockUp<TotpProviderImpl>() {};

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.auth.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(subject, callbackHandler, sharedState, options);
        loginModule.login();
        fail("例外が発生するはず");
    }

    /**
     * 006: void login() のテストメソッドです. CallbackHandlerがUnsupportedCallbackExceptionをthrowした場合
     * RuntimeExceptionがthrowされることをテストします。
     * @throws LoginException 認証エラーの場合
     */
    @Test(expected = RuntimeException.class)
    public void testLogin002() throws LoginException {

        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                throw new UnsupportedCallbackException(null);
            }
        };
        new MockUp<TotpProviderImpl>() {};

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.auth.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(subject, callbackHandler, sharedState, options);
        loginModule.login();
        fail("例外が発生するはず");
    }

    /**
     * 007: void login() のテストメソッドです. TotpProviderがTotpGenerationExceptionをthrowした場合
     * RuntimeExceptionがthrowされることをテストします。
     * @throws LoginException 認証エラーの場合
     */
    @Test(expected = RuntimeException.class)
    public void testLogin003() throws LoginException {
        final String userName = "user001";
        final String oneTimePwd = "totp";
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(oneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                throw new TotpGenerationException();
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.auth.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(subject, callbackHandler, sharedState, options);
        loginModule.login();
        fail("例外が発生するはず");
    }

    /**
     * 008: void login() のテストメソッドです. 認証成功の場合、trueが返却されることをテストします。
     */
    @Test
    public void testLogin004() {
        final String userName = "user001";
        final String immutableUserId = "001";
        final String oneTimePwd = "totp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(oneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                Totp totp = new Totp(immutableUserId, timeCount, oneTimePwd);
                TotpCache.getInstance().add(immutableUserId, totp);
                return totp;
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(subject, callbackHandler, sharedState, options);
        boolean actual = false;
        try {
            actual = loginModule.login();
        } catch (Exception e) {
            fail("例外は発生しないはず");
        }
        assertTrue(actual);
    }

    /**
     * 009: void login() のテストメソッドです. 認証失敗の場合、FaildLoginExceptionがthrowされることをテストします。
     * @throws LoginException 認証エラーの場合
     */
    @Test(expected = FailedLoginException.class)
    public void testLogin005() throws LoginException {
        final String userName = "user001";
        final String immutableUserId = "001";
        final String oneTimePwd = "totp";
        final String incorrectOneTimePwd = "incorrectTotp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(incorrectOneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                return new Totp(immutableUserId, timeCount, oneTimePwd);
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject subject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(subject, callbackHandler, sharedState, options);
        loginModule.login();
        fail("例外が発生するはず");
    }

    /**
     * 010: void commit() のテストメソッドです.
     * login()結果が成功の場合、SubjectにUserPrincipalが追加され、trueが返却されることをテストします。
     */
    @Test
    public void testCommit001() {
        final String userName = "user002";
        final String immutableUserId = "002";
        final String oneTimePwd = "totp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(oneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                Totp totp = new Totp(immutableUserId, timeCount, oneTimePwd);
                TotpCache.getInstance().add(immutableUserId, totp);
                return totp;
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject initialSubject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        boolean loginSuccess = false;
        boolean commitSuccess = false;
        try {
            loginSuccess = loginModule.login();
            commitSuccess = loginModule.commit();
        } catch (Exception e) {
            fail("例外は発生しないはず");
        }
        assertTrue(loginSuccess && commitSuccess);

        Subject subject = null;
        try {
            Field subjectField = loginModule.getClass().getDeclaredField("_subject");
            subjectField.setAccessible(true);
            subject = (Subject) subjectField.get(loginModule);
        } catch (Exception e) {
            fail("例外は発生しないはず");
        }
        UserPrincipal principal = (UserPrincipal) subject.getPrincipals().iterator().next();
        assertEquals(userName, principal.getName());
    }

    /**
     * 011: void commit() のテストメソッドです. login()結果が成功で、commit()が失敗した場合LoginExceptionがthrowされることをテストします。
     * @throws LoginException 認証エラーの場合
     */
    @Test(expected = LoginException.class)
    public void testCommit002() throws LoginException {
        final String userName = "user003";
        final String immutableUserId = "003";
        final String oneTimePwd = "totp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(oneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                Totp totp = new Totp(immutableUserId, timeCount, oneTimePwd);
                TotpCache.getInstance().add(immutableUserId, totp);
                return totp;
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject initialSubject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        // Subjectを読取専用にしてcommit()を失敗させる
        initialSubject.setReadOnly();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        loginModule.login();
        loginModule.commit();
        fail("例外が発生するはず");
    }

    /**
     * 012: void commit() のテストメソッドです. login()結果が失敗で、commit()が成功した場合falseが返却されることをテストします。
     */
    @Test
    public void testCommit003() {
        final String userName = "user004";
        final String immutableUserId = "004";
        final String oneTimePwd = "totp";
        final String incorrectOneTimePwd = "incorrectTotp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(incorrectOneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                return new Totp(immutableUserId, timeCount, oneTimePwd);
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject initialSubject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        boolean loginSuccess = false;
        boolean commitSuccess = false;
        try {
            loginSuccess = loginModule.login();
            commitSuccess = loginModule.commit();
            fail("例外が発生するはず");
        } catch (Exception e) {
            assertEquals(FailedLoginException.class, e.getClass());
        }
        assertFalse(loginSuccess);
        assertFalse(commitSuccess);
    }

    /**
     * 013: void commit() のテストメソッドです. login()結果が失敗で、commit()が失敗する条件の場合falseが返却されることをテストします。
     */
    @Test
    public void testCommit004() {
        final String userName = "user004";
        final String immutableUserId = "004";
        final String oneTimePwd = "totp";
        final String incorrectOneTimePwd = "incorrectTotp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(incorrectOneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                return new Totp(immutableUserId, timeCount, oneTimePwd);
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject initialSubject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        // Subjectを読取専用にしてcommit()を失敗させる
        initialSubject.setReadOnly();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        boolean loginSuccess = false;
        boolean commitSuccess = false;
        try {
            loginSuccess = loginModule.login();
            commitSuccess = loginModule.commit();
            fail("例外が発生するはず");
        } catch (Exception e) {
            assertEquals(FailedLoginException.class, e.getClass());
        }
        assertFalse(loginSuccess);
        assertFalse(commitSuccess);
    }

    /**
     * 014: void abort() のテストメソッドです. login()結果が成功、commit()結果が成功、abort()が成功する場合 trueが返却されることをテストします。
     */
    @Test
    public void testAbort001() {
        final String userName = "user005";
        final String immutableUserId = "005";
        final String oneTimePwd = "totp";
        final long timeCount = 100L;
        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };
        new MockUp<ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(oneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                Totp totp = new Totp(immutableUserId, timeCount, oneTimePwd);
                TotpCache.getInstance().add(immutableUserId, totp);
                return totp;
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();

        Subject initialSubject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        boolean loginSuccess = false;
        boolean commitSuccess = false;
        boolean abortSuccess = false;
        try {
            loginSuccess = loginModule.login();
            commitSuccess = loginModule.commit();
            abortSuccess = loginModule.abort();
        } catch (Exception e) {
            assertEquals(FailedLoginException.class, e.getClass());
        }
        assertTrue(loginSuccess);
        assertTrue(commitSuccess);
        assertTrue(abortSuccess);
    }

    /**
     * 015: void abort() のテストメソッドです. login()結果が成功、commit()結果が失敗、abort()が成功する場合 trueが返却されることをテストします。
     */
    @Test
    public void testAbort002() {
        final String userName = "user006";
        final String immutableUserId = "006";
        final String oneTimePwd = "totp";
        final long timeCount = 100L;

        new MockUp<ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(oneTimePwd);
            }
        };

        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                Totp totp = new Totp(immutableUserId, timeCount, oneTimePwd);
                TotpCache.getInstance().add(immutableUserId, totp);
                return totp;
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();

        Subject initialSubject = new Subject();
        // Subjectを読取専用にしてcommit()を失敗させる
        initialSubject.setReadOnly();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        boolean loginSuccess = false;
        boolean abortSuccess = false;
        try {
            loginSuccess = loginModule.login();
            loginModule.commit();
            fail("例外が発生するはず");
        } catch (Exception e) {
            assertEquals(LoginException.class, e.getClass());
        }
        try {
            abortSuccess = loginModule.abort();
        } catch (LoginException e) {
            fail("例外は発生しないはず");
        }
        assertTrue(loginSuccess);
        assertTrue(abortSuccess);
    }

    /**
     * 016: void abort() のテストメソッドです. login()結果が成功、commit()結果が成功、abort()が失敗する場合
     * LoginExceptionがthrowされることをテストします。
     * @throws LoginException 認証エラーの場合
     */
    @Test(expected = LoginException.class)
    public void testAbort003() throws LoginException {
        final String userName = "user007";
        final String immutableUserId = "007";
        final String oneTimePwd = "totp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(oneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                Totp totp = new Totp(immutableUserId, timeCount, oneTimePwd);
                TotpCache.getInstance().add(immutableUserId, totp);
                return totp;
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject initialSubject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        boolean loginSuccess = false;
        boolean commitSuccess = false;
        try {
            loginSuccess = loginModule.login();
            commitSuccess = loginModule.commit();
        } catch (Exception e) {
            fail("例外は発生しないはず");
        }
        assertTrue(loginSuccess);
        assertTrue(commitSuccess);
        // Subjectを読取専用にしてabort()を失敗させる
        initialSubject.setReadOnly();
        loginModule.abort();
        fail("例外が発生するはず");
    }

    /**
     * 017: void abort() のテストメソッドです. login()結果が成功、commit()結果が失敗、abort()が失敗する場合
     * LoginExceptionがthrowされることをテストします。
     */
    @Test
    public void testAbort004() {
        // commit()失敗時のabort()はインスタンス変数にnullを代入するのみのため
        // abort()失敗する状況を作り出せず、テストできません。
    }

    /**
     * 018 void abort() のテストメソッドです. login()結果が失敗、commit()結果が成功、abort()が成功の場合 falseが返却されることをテストします。
     */
    @Test
    public void testAbort005() {
        final String userName = "user008";
        final String immutableUserId = "008";
        final String oneTimePwd = "totp";
        final String incorrectOneTimePwd = "incorrectTotp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(incorrectOneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                return new Totp(immutableUserId, timeCount, oneTimePwd);
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();
        Subject initialSubject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        boolean commitSuccess = false;
        boolean abortSuccess = false;
        try {
            loginModule.login();
            fail("例外が発生するはず");
        } catch (Exception e) {
            assertEquals(FailedLoginException.class, e.getClass());
        }
        try {
            commitSuccess = loginModule.commit();
            abortSuccess = loginModule.abort();
        } catch (LoginException e) {
            fail("例外は発生しないはず");
        }
        assertFalse(commitSuccess);
        assertFalse(abortSuccess);
    }

    /**
     * 019 void abort() のテストメソッドです. login()結果が失敗、commit()結果が失敗、abort()が成功の場合 falseが返却されることをテストします。
     */
    @Test
    public void testAbort006() {
        // commit()失敗時のabort()はインスタンス変数にnullを代入するのみのため
        // abort()失敗する状況を作り出せず、テストできません。
    }

    /**
     * 020: void abort() のテストメソッドです. login()結果が失敗、commit()結果が失敗、abort()が成功の場合 falseが返却されることをテストします。
     */
    @Test
    public void testAbort007() {
        final String userName = "user008";
        final String immutableUserId = "008";
        final String oneTimePwd = "totp";
        final String incorrectOneTimePwd = "incorrectTotp";
        final long timeCount = 100L;
        new MockUp<UsernameTotpAuthnCallbackHandler>() {
            @Mock
            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                ((NameCallback) callbacks[0]).setName(userName);
                ((OneTimePasswordCallback) callbacks[1]).setOneTimePassword(incorrectOneTimePwd);
            }
        };
        new MockUp<TotpProviderImpl>() {
            @Mock
            Totp getTotp(String userName) throws TotpGenerationException {
                Totp totp = new Totp(immutableUserId, timeCount, oneTimePwd);
                TotpCache.getInstance().add(userName, totp);
                return totp;
            }
        };

        ValidateUsernameTotpAction action = new ValidateUsernameTotpAction();
        ValidateUsernameTotpAction.UsernameTotpAuthnCallbackHandler callbackHandler = action.new UsernameTotpAuthnCallbackHandler();

        Subject initialSubject = new Subject();
        Map<String, ?> sharedState = new HashMap();
        Map<String, String> options = new HashMap();
        options.put("TotpProviderClass", "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl");

        TotpLoginModule loginModule = new TotpLoginModule();
        loginModule.initialize(initialSubject, callbackHandler, sharedState, options);
        boolean commitSuccess = false;
        boolean abortSuccess = false;
        try {
            loginModule.login();
            fail("例外が発生するはず");
        } catch (Exception e) {
            assertEquals(FailedLoginException.class, e.getClass());
        }
        try {
            initialSubject.setReadOnly();
            commitSuccess = loginModule.commit();
            abortSuccess = loginModule.abort();
        } catch (LoginException e) {
            fail("例外は発生しないはず");
        }
        assertFalse(commitSuccess);
        assertFalse(abortSuccess);
    }

    /**
     * 021: void abort() のテストメソッドです. login()結果が失敗、commit()結果が失敗、abort()が失敗の場合 falseが返却されることをテストします。
     */
    @Test
    public void testAbort008() {
        // commit()失敗時のabort()はインスタンス変数にnullを代入するのみのため
        // abort()失敗する状況を作り出せず、テストできません。
    }
}
