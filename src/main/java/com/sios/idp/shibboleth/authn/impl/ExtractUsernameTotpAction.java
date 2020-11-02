/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.impl;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.authn.context.UsernameTotpContext;

import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * HTTPのformのbodyやquery stringからユーザー名、ワンタイムパスワードの入力値を抽出し、 抽出したものを
 * {@link UsernameTotpContext}に格納して、 {@link AuthenticationContext}に設定します。
 *
 * @author SIOS Technology, Inc.
 */
public class ExtractUsernameTotpAction extends AbstractExtractionAction {
    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /** ユーザ名のパラメータ名. */
    @Nonnull
    @NotEmpty
    private String usernameFieldName;

    /** ワンタイムパスワードのパラメータ名. */
    @Nonnull
    @NotEmpty
    private String totpFieldName;

    /** SSOバイパスのパラメータ名. */
    @Nonnull
    @NotEmpty
    private String ssoBypassFieldName;

    /** TOTP認証セッション信頼フラグのパラメータ名. */
    @Nonnull
    @NotEmpty
    private String trustsTotpAuthnSessionFieldName;

    private String name;

    /**
     * コンストラクタ.
     */
    public ExtractUsernameTotpAction() {
        /** Constructor. */
        usernameFieldName = "username";
        totpFieldName = "totp";
        ssoBypassFieldName = "donotcache";
        trustsTotpAuthnSessionFieldName = "truststotpauthnsession";
    }

    /**
     * ユーザ名のパラメータ名を設定します.
     * @param fieldName ユーザ名のパラメータ名
     */
    public void setUsernameFieldName(@Nonnull @NotEmpty final String fieldName) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        usernameFieldName = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "ユーザ名のフィールド名が設定されていません.");

    }

    /**
     * ワンタイムパスワードのパラメータ名.
     * @param fieldName ワンタイムパスワードのパラメータ名
     */
    public void setTotpFieldName(@Nonnull @NotEmpty final String fieldName) {

        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        totpFieldName = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "ワンタイムトークンのフィールド名が設定されていません.");
    }

    /**
     * SSOバイパスのパラメータ名の設定.
     * @param fieldName SSOバイパスのパラメータ名
     */
    public void setSSOBypassFieldName(@Nonnull @NotEmpty final String fieldName) {

        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
 

        ssoBypassFieldName = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "シングルサインオンバイパスのフィールド名が設定されていません.");
    }

    /**
     * TOTP認証セッション信頼フラグのパラメータ名の設定.
     * @param fieldName TOTP認証セッション信頼フラグのパラメータ名
     */
    public void setTrustsTotpAuthnSessionFieldName(@Nonnull @NotEmpty final String fieldName) {

        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);


        trustsTotpAuthnSessionFieldName = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "TOTP認証セッション信頼フラグのフィールド名が設定されていません.");
    }

    /** {@inheritDoc} */
    public void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        final UsernameTotpContext upCtx = authenticationContext.getSubcontext(
                UsernameTotpContext.class, true);

        upCtx.setUsername(null);

        upCtx.setTotp(null);

        upCtx.setTrustsTotpAuthnSession(false);
        final HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }
        // ユーザ名
        final String username = request.getParameter(usernameFieldName);
        if (username == null || username.isEmpty()) {
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }
        upCtx.setUsername(applyTransforms(username));

        // TOTP
        final String totp = request.getParameter(totpFieldName);
        if (totp == null || totp.isEmpty()) {
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }
        upCtx.setTotp(totp);

        // 同一セッションで別SPアクセス時に再度認証を要求する/しない（認証結果をキャッシュしない場合は認証を要求される）
        final String donotcache = request.getParameter(ssoBypassFieldName);
        if ("1".equals(donotcache)) {
            authenticationContext.setResultCacheable(false);
        }

        // TOTP認証セッション信頼フラグ
        final String trustsTotpAuthnSession = request.getParameter(trustsTotpAuthnSessionFieldName);
        upCtx.setTrustsTotpAuthnSession("1".equals(trustsTotpAuthnSession));
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
