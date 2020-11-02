/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.jaas;

import java.io.IOException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.authn.totp.Totp;
import com.sios.idp.shibboleth.authn.totp.TotpCache;
import com.sios.idp.shibboleth.authn.totp.TotpProvider;
import com.sios.idp.shibboleth.common.util.ExceptionUtil;
import com.sios.idp.shibboleth.exception.TotpGenerationException;
import com.sios.idp.shibboleth.exception.TotpProviderInstantiationException;
import com.sun.security.auth.UserPrincipal;

/**
 * JAAS LoginModule の Time-based One-Time Password (TOTP) 認証の実装クラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpLoginModule implements LoginModule {

    /** Class logger. */
    private final Logger _logger = LoggerFactory.getLogger(this.getClass());

    /** login.configのオプション・キー名「TotpProvider実装クラス名」. */
    private static final String TOTP_PROVIDER_CLASS = "TotpProviderClass";

    /** CallbackHandler. */
    private CallbackHandler _callbackHandler;

    /** LoginModule間の共有情報. */
    @SuppressWarnings("unused")
    private Map<String, ?> _sharedState;

    /** login.configに定義されたオプション. */
    private Map<String, ?> _options;

    /** Subject. */
    private Subject _subject;
    /** User principal. */
    private UserPrincipal userPrincipal;

    // Configurable options

    /** TOTP生成クラス. */
    private TotpProvider totpProvider;
    /** ユーザ名. */
    private String userName;
    /** ワンタイムパスワード. */
    private String oneTimePassword;

    /** 認証第1フェーズの成否. */
    private boolean succeeded = false;
    /** 認証第2フェーズの成否. */
    private boolean commitSucceeded = false;

    @Override
    /** @inheritDoc */
    public void initialize(Subject subject,
             CallbackHandler callbackHandler,
             Map<String, ?> sharedState,  Map<String, ?> options) {

        this._subject = subject;
        this._callbackHandler = callbackHandler;
        this._sharedState = sharedState;
        this._options = options;

        try {
            setConfigurableOptions();
        } catch (TotpProviderInstantiationException e) {
            // Override元のメソッドでthrowsが定義されていないため、
            // 及び回復不能なエラーのためRuntimeExceptionをthrow
            throw new RuntimeException(e);
        }
    }

    @Override
    /** @inheritDoc */
    public boolean login() throws LoginException {

        // 認証に使用するINPUT情報取得
        getAuthInputFromCallbacks();

        // TOTP期待値の生成
        Totp expectedTotp = null;
        try {
            expectedTotp = totpProvider.getTotp(userName);
            _logger.debug("TOTPを取得しました。 (ユーザ名：{}, TOTP：{})", userName, expectedTotp.getTotp());
        } catch (TotpGenerationException e) {
            handleException(e, "TOTPの取得に失敗しました。ユーザ名：{0}", userName);
        }

        // ユーザ固有IDの取得
        String immutableUserId = expectedTotp.getImmutableUserId();

        // TOTP認証
        Totp inputtedTotp = new Totp(
                immutableUserId, expectedTotp.getTimeCounter(), oneTimePassword);
        TotpCache totpCache = TotpCache.getInstance();
        succeeded =  totpCache.isAvailable(immutableUserId, inputtedTotp);

        if (succeeded) {
            expectedTotp.isAuthenticated(true);
        } else {
            throw new FailedLoginException("TOTP認証エラー");
        }
        _logger.debug(MessageFormat.format("TOTP 1次認証フェーズ結果：{0}", succeeded));
        return succeeded;
    }

    @Override
    /** @inheritDoc */
    public boolean commit() throws LoginException {

        if (!succeeded) {
            clearPrivateAuthInfo();
            _logger.debug(MessageFormat.format("TOTP 2次認証フェーズ結果：{0}", false));
            return false;
        }

        checkSubjectReadOnly();

        if (succeeded) {
            userPrincipal = new UserPrincipal(userName);
            Set<Principal> principals = _subject.getPrincipals();
            if (!principals.contains(userPrincipal)) {
                principals.add(userPrincipal);
            }
            clearPrivateAuthInfo();
            commitSucceeded  = true;
        }
        _logger.debug(MessageFormat.format("TOTP 2次認証フェーズ結果：{0}", true));
        return true;
    }

    @Override
    /** @inheritDoc */
    public boolean abort() throws LoginException {

        if (!succeeded) { return false; }

        if (commitSucceeded) {
            logout();
        } else {
            clearPrivateAuthInfo();
            clearPrivateAuthResult();
        }
        return true;
    }

    @Override
    /** @inheritDoc */
    public boolean logout() throws LoginException {
        clearPrivateAuthResult();
        checkSubjectReadOnly();
        clearPrincipal();
        return true;
    }

    /**
     * ログイン構成ファイルに定義されたオプションを設定します.
     * @throws TotpProviderInstantiationException TOTPプロバイダの実装クラスのインスタンス生成に失敗した場合
     */
    private void setConfigurableOptions() throws TotpProviderInstantiationException {
        totpProvider = createTotpProviderInstance(_options);
    }

    /**
     * ログイン構成ファイルの必須オプションを取得します.
     * @param optionKey オプションキー名
     * @return オブジェクト型のオプション値
     */
    private Object getRequiredOptionValue(String optionKey) {

        Object o = null;
        if (_options.containsKey(optionKey)) {
            o = _options.get(optionKey);
        }
        if (o == null) {
            // Override元のメソッドでthrowsが定義されていないためRuntimeExceptionをthrow
            throw new RuntimeException(MessageFormat.format(
                    "JAASログイン構成ファイルの必須パラメータ {0} が定義されていません。", optionKey));
        }
        return o;
    }

    /**
     * {@link com.sios.shibboleth.idp.auth.totp.TotpProvider}の実装クラスを生成します.
     * @param options ログイン構成ファイルオプションのマップ
     * @return TOTPプロバイダ
     * @throws TotpProviderInstantiationException JAASログイン構成ファイルに定義されているTOTPプロバイダのインスタンス生成に失敗した場合
     */
    private TotpProvider createTotpProviderInstance(Map<String, ?> options)
            throws TotpProviderInstantiationException {

        String className = (String) getRequiredOptionValue(TOTP_PROVIDER_CLASS);
        Object o;
        try {
            o = Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new TotpProviderInstantiationException(MessageFormat.format(
                    "TOTPプロバイダ「{0}」のインスタンス生成に失敗しました。", className));
        }
        if (!(o instanceof TotpProvider)) {
            throw new TotpProviderInstantiationException(MessageFormat.format(
                    "JAASログイン構成ファイルのプロパティ「{0}：{1}」が {2} を実装していません。",
                    TOTP_PROVIDER_CLASS, className, TotpProvider.class.getName()));
        }
        return (TotpProvider) o;
    }

    /**
     * {@link javax.security.auth.callback.Callback}から認証INPUT情報を取得します.
     */
    private void getAuthInputFromCallbacks()  {

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("username: ");
        callbacks[1] = new OneTimePasswordCallback("one-time password: ");
        try {
            _callbackHandler.handle(callbacks);
        } catch (IOException e) {
            handleException(e, "Callbackからの入力情報取得時にjava.io.IOExceptionが発生しました。");
        } catch (UnsupportedCallbackException e) {
            handleException(e, "未サポートのCallbackが指定されました。");
        }
        userName = ((NameCallback) callbacks[0]).getName();
        OneTimePasswordCallback otpCallback = (OneTimePasswordCallback) callbacks[1];
        oneTimePassword = otpCallback.getOneTimePassword();
        otpCallback.clearOneTimePassword();
    }

    /**
     * {@link javax.security.auth.Subject}が読取専用であるかをチェックします.
     * @throws LoginException Subjectが読取専用の場合
     */
    private void checkSubjectReadOnly() throws LoginException {
        if (_subject.isReadOnly()) {
            clearPrivateAuthInfo();
            throw new LoginException("認証サブジェクトが読み取り専用です");
        }
    }

    /**
     * 状態をクリアします.
     */
    private void clearPrincipal() {
        Set<Principal> principals = _subject.getPrincipals();
        if (principals.contains(userPrincipal)) {
            principals.remove(userPrincipal);
        }
        userPrincipal = null;
    }

    /**
     * 状態をクリアします.
     */
    private void clearPrivateAuthInfo() {
        userName = null;
        oneTimePassword = null;
    }

    /**
     * 内部的に保持している認証結果をクリアします.
     */
    private void clearPrivateAuthResult() {
        succeeded = false;
        commitSucceeded = false;
    }


    /**
     * 回復不可能な例外としてハンドリングします.
     * 指定されたメッセージのエラーログを出力し、{@link java.lang.RuntimeException}をRe-throwします。
     * @param e 例外
     * @param message メッセージ
     * @param messageArgs メッセージ埋め込み文字列
     */
    private void handleException(Exception e, String message, Object...messageArgs) {
        _logger.error(MessageFormat.format(message, messageArgs));
        _logger.error(ExceptionUtil.stackTraceToString(e));
        throw new RuntimeException(e);
    }
}
