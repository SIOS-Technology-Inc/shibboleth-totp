/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.impl;

import java.security.NoSuchAlgorithmException;
import java.security.URIParameter;
import java.text.MessageFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.authn.context.UsernameTotpContext;
import com.sios.idp.shibboleth.authn.jaas.OneTimePasswordCallback;
import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.common.util.ExceptionUtil;
import com.sios.idp.shibboleth.common.util.TotpAuthnSessionIdGenerator;
import com.sios.idp.shibboleth.exception.TotpAuthnSessionIdGenerationException;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * TOTPバリデートアクションクラスです. JAASによって起動され、{@link UsernameTotpContext}をチェックするアクション.直接
 * {@link net.shibboleth.idp.authn.AuthenticationResult}を生成する
 * <p>
 * JAASのコンフィギュレーションプロセスをコントロールするためのオプションのプロパティが サポートされています。 ログインに成功した場合、
 * {@link net.shibboleth.idp.authn.AuthenticationResult} は {@link AuthenticationContext} に 保存されます。
 * ログインに失敗した場合、handleError(ProfileRequestContext
 * , AuthenticationContext, Exception, String)}
 * メソッドが呼び出されます。
 * @author SIOS Technology, Inc.
 */
public class ValidateUsernameTotpAction extends AbstractUsernameTotpValidationAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /** JAAS定義のタイプ. */
    @Nullable
    private String loginConfigType;

    /** JAAS定義のタイプ固有のパラメータ. */
    @Nullable
    private Configuration.Parameters loginConfigParameters;

    /** コンストラクタ. */
    public ValidateUsernameTotpAction() {
    }

    /**
     * 使用するJAASの定義 {@link Configuration}のタイプを取得.
     * @return 使用するJAASのタイプ
     */
    @Nullable
    public final String getLoginConfigType() {
        return loginConfigType;
    }

    /**
     * 使用するJAASの定義 {@link Configuration} のタイプを設定.
     * @param type 使用するJAASの定義タイプ
     */
    public final void setLoginConfigType(@Nullable final String type) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        loginConfigType = Constraint.isNotNull(StringSupport.trimOrNull(type),
                "LoginConfigType  cannot be null or empty.");

    }

    /**
     * 使用するJAASの定義 {@link Configuration} 特有のパラメータを取得.
     * @return 使用するJAASの定義特有のパラメータ
     */
    @Nullable
    public Configuration.Parameters getLoginConfigParameters() {
        return loginConfigParameters;
    }

    /**
     * 使用するJAAS定義 {@link Configuration} 特有のパラメータを設定.
     * @param params 使用するJAAS定義特有のパラメータ
     */
    public void setLoginConfigParameters(@Nullable Configuration.Parameters params) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        loginConfigParameters = params;
        log.info(MessageFormat.format("JAAS ログインモジュール設定ファイル：{0}"
                , ((URIParameter) loginConfigParameters).getURI().getPath()));
    }

    /** {@inheritDoc} */
    @Override
    public void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        String loginConfigName = "";
        HttpServletRequest request = getHttpServletRequest();

        log.info("TOTP認証を利用します。");
        loginConfigName = getTotpJaasLoginConfigName();
        try {
            authenticate(loginConfigName);
            log.info("ログイン成功：{} (from {})", getUsernameTotpContext().getUsername(), request.getRemoteAddr());
            if (AppConfig.getTotpAuthnSessionExpirationSec() > 0
                    && getUsernameTotpContext().trustsTotpAuthnSession()) {
                try {
                    log.info("TOTP認証セッションを信頼し、セッションIDをCookieに記録します。");
                    trustTotpAuthnSession();
                } catch (TotpAuthnSessionIdGenerationException e) {
                    log.warn("TOTP認証セッションIDの生成に失敗しました。TOTP認証セッションの信頼を中止します。");
                }
            } else {
                log.info("TOTP認証セッション生成を信頼しません。");
            }
            buildAuthenticationResult(profileRequestContext, authenticationContext);
            ActionSupport.buildProceedEvent(profileRequestContext);
            return;
        } catch (LoginException e) {
            log.error("ログイン失敗 ({})：{} (from {}, {}, {})", "認証エラー", getUsernameTotpContext().getUsername(),
                    request.getRemoteAddr(), e.getMessage(), ExceptionUtil.stackTraceToString(e));
            handleError(profileRequestContext, authenticationContext, "AuthenticationException",
                    AuthnEventIds.AUTHN_EXCEPTION);
        } catch (Exception e) {
            log.error("ログイン失敗 ({})：{} (from {}, {}, {})", "予期せぬエラー", getUsernameTotpContext().getUsername(),
                    request.getRemoteAddr(), e.getMessage(), ExceptionUtil.stackTraceToString(e));
            throw new RuntimeException(e);

        }

    }

    /**
     * JAAS定義を生成し、JAAS定義に従ってログインをします.
     * @param loginConfigName 使用されるログイン定義名
     * @throws LoginException JAASログインプロセスが失敗した場合にスローされる
     * @throws NoSuchAlgorithmException JAAS定義が生成されなかった場合にスローされる
     */
    private void authenticate(@Nonnull @NotEmpty final String loginConfigName) throws LoginException,
            NoSuchAlgorithmException {

        javax.security.auth.login.LoginContext jaasLoginCtx;

        HttpServletRequest request = getHttpServletRequest();

        UsernameTotpContext utContext = getUsernameTotpContext();

        log.info("認証を開始します。 (ユーザ名：{}, 接続元IP：{}, 認証方式：{})", utContext.getUsername(),
                request.getRemoteAddr(), "TOTP");

        Configuration loginConfig = Configuration.getInstance(getLoginConfigType(), getLoginConfigParameters());
        jaasLoginCtx = new javax.security.auth.login.LoginContext(loginConfigName, getSubject(),
                new UsernameTotpAuthnCallbackHandler(), loginConfig);

        jaasLoginCtx.login();
    }

    /**
     * 現在時刻からTOTP認証セッションIDを生成し、処理日時とともにCookieに保存します.
     * 既にCookieに保存されている認証セッションID、処理日時は削除されます.
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションIDの生成に失敗した場合
     */
    private void trustTotpAuthnSession() throws TotpAuthnSessionIdGenerationException {
        long issueDate = System.currentTimeMillis() / 1000L;
        String sessionId = null;
        String issueDateString = String.valueOf(issueDate);
        sessionId = TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(
                getUsernameTotpContext().getUsername(), issueDateString);
        log.debug("TOTP認証セッションID : {}", sessionId);

        HttpServletRequest request = getHttpServletRequest();
        HttpServletResponse response = getHttpServletResponse();
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                final String cookieName = cookie.getName();
                if (AppConfig.getTotpAuthnSessionIdCookieName().equals(cookieName)) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                } else if (AppConfig.getTotpAuthnSessionIssueDateCookieName().equals(cookieName)) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        Cookie cookie = new Cookie(AppConfig.getTotpAuthnSessionIdCookieName(), sessionId);
        cookie.setMaxAge(AppConfig.getTotpAuthnSessionExpirationSec());
        response.addCookie(cookie);
        cookie = new Cookie(AppConfig.getTotpAuthnSessionIssueDateCookieName(), issueDateString);
        cookie.setMaxAge(AppConfig.getTotpAuthnSessionExpirationSec());
        response.addCookie(cookie);
    }

    /**
     * 名前、OnetimeパスワードのデータをJAAS loginプロセスに提供するコールバックハンドラ.
     * このハンドラは、名前のコールバック、ワンタイムパスワードのコールバックを提供する
     */
    public class UsernameTotpAuthnCallbackHandler implements CallbackHandler {

        /**
         * コールバックハンドラ.
         * @param callbacks 処理するためのコールバックのリスト.
         * @throws UnsupportedCallbackException コールバックが {@link NameCallback} 、
         *             {@link OneTimePasswordCallback}以外が呼ばれた場合例外をスローする
         */
        public void handle(final Callback[] callbacks) throws UnsupportedCallbackException {

            if (callbacks == null || callbacks.length == 0) { return; }

            for (Callback cb : callbacks) {
                if (cb instanceof NameCallback) {
                    NameCallback ncb = (NameCallback) cb;
                    ncb.setName(getUsernameTotpContext().getUsername());
                } else if (cb instanceof OneTimePasswordCallback) {
                    OneTimePasswordCallback ocb = (OneTimePasswordCallback) cb;
                    ocb.setOneTimePassword(getUsernameTotpContext().getTotp());
                }
            }
        }
    }
}
