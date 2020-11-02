/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.authn.context.UsernameTotpContext;

import net.shibboleth.idp.authn.AbstractValidationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Totp認証用抽象アクションクラス. {@link UsernameTotpContext}をチェックし、
 * {@link net.shibboleth.idp.authn.AuthenticationResult}を生成する、
 * AuthenticationContext.getSubcontext(UsernameTotpContext.class) != nullの場合、 ログインが成功すると、
 * {@link net.shibboleth.idp.authn.AuthenticationResult} は {@link AuthenticationContext} に 保存される。
 * ログインに失敗すると、
 * {@link AbstractValidationAction#handleError(ProfileRequestContext, AuthenticationContext, Exception, String)}
 * メソッドが呼び出される
 * @author SIOS Technology, Inc.
 */
public abstract class AbstractUsernameTotpValidationAction
extends AbstractValidationAction {
    /** Class logger. */
    private final Logger _logger = LoggerFactory.getLogger(this.getClass());

    /** ユーザパスワードワンタイムパスワードを格納したコンテキストクラス. */
    private com.sios.idp.shibboleth.authn.context.UsernameTotpContext utContext;

    /** TOTP認証のJAASログイン定義名. */
    @Nonnull
    @NotEmpty
    private String totpJaasLoginConfigName;

    /**
     * Totp認証のJAASログイン定義名を取得します.
     * @return Totp認証のJAASログイン定義名
     */
    @Nullable
    public String getTotpJaasLoginConfigName() {
        return totpJaasLoginConfigName;
    }

    /**
     * TOTP認証のJAASログイン定義名の設定.
     * @param inTotpJaasLoginConfigName TOTP認証のJAASログイン定義名
     */
    public void setTotpJaasLoginConfigName(@Nullable String inTotpJaasLoginConfigName) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        totpJaasLoginConfigName = StringSupport.trimOrNull(inTotpJaasLoginConfigName);
    }

    /**
     * Totp認証のコンテキストの取得.
     * @return Totp認証用コンテキスト
     */
    @Nullable
    public UsernameTotpContext getUsernameTotpContext() {
        return utContext;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        if (!super.doPreExecute(profileRequestContext, authenticationContext)) { return false; }
        utContext = authenticationContext.getSubcontext(UsernameTotpContext.class);
        HttpServletRequest request = getHttpServletRequest();

        if (utContext == null) {
            _logger.error("ログイン失敗 ({})：{} (from {}, {}, {})", "認証エラー", "", request.getRemoteAddr(),
                    "必須項目が入力されていません。", "");
            handleError(profileRequestContext, authenticationContext, "NoCredentials", AuthnEventIds.NO_CREDENTIALS);
            return false;
        } else if (utContext.getUsername() == null) {
            _logger.error("ログイン失敗 ({})：{} (from {}, {}, {})", "認証エラー", "", request.getRemoteAddr(),
                    "必須項目が入力されていません。", "");
            handleError(profileRequestContext, authenticationContext, "NoCredentials", AuthnEventIds.NO_CREDENTIALS);
            return false;
        } else if (utContext.getTotp() == null) {
            _logger.error("ログイン失敗 ({})：{} (from {}, {}, {})", "認証エラー", utContext.getUsername(),
                request.getRemoteAddr(), "必須項目が入力されていません。", "");
            handleError(profileRequestContext, authenticationContext, "NoCredentials", AuthnEventIds.NO_CREDENTIALS);
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    protected Subject populateSubject(@Nonnull final Subject subject) {
        subject.getPrincipals().add(new UsernamePrincipal(utContext.getUsername()));
        return subject;
    }
}
