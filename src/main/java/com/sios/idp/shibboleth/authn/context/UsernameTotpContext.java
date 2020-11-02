/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.messaging.context.BaseContext;

/**
 * TOTPのコンテキストクラスです.
 * いつも{@link net.shibboleth.idp.authn.context.AuthenticationContext}
 * に設定されます。
 * ユーザ名、ワンタイムパスワードを格納します。
 * @author SIOS Technology, Inc.
 */
public class UsernameTotpContext extends BaseContext {

    /** ユーザー名. */
    private String username;
    /** ユーザー名にひもづくワンタイムパスワード. */
    private String totp;
    /** TOTP認証セッション信頼フラグ(true=信頼し、Cookieを発行する, false=信頼しない). */
    private boolean trustsTotpAuthnSession;
    /**
     * ユーザ名の取得.
     * @return ユーザ名
     */
    @Nullable public String getUsername() {
        return username;
    }
    /**
     * ユーザ名の設定.
     * @param name ユーザ名文字列
     * @return 本コンテキスト
     */
    @Nonnull public UsernameTotpContext setUsername(@Nullable final String name) {
        username = name;
        return this;
    }
    /**
     * ユーザ名に紐づくワンタイムパスワードの取得.
     * @return ユーザ名に紐づくワンタイムパスワード
     */
    @Nullable public String getTotp() {
        return totp;
    }
    /**
     * ユーザ名に紐づくワンタイムパスワードの設定.
     * @param inTotp ワンタイムパスワード
     * @return 本コンテキスト
     */
    @Nonnull public UsernameTotpContext setTotp(@Nullable final String inTotp) {
        totp = inTotp;
        return this;
    }
    /**
     * TOTP認証セッション信頼フラグの取得.
     * @return TOTP認証セッション信頼フラグ(true=信頼し、Cookieを発行する, false=信頼しない).
     */
    public boolean trustsTotpAuthnSession() {
        return trustsTotpAuthnSession;
    }
    /**
     * TOTP認証セッション信頼フラグの設定.
     * @param trustsSession TOTP認証セッションを信頼し、Cookieを発行する=true, 信頼しない=false
     * @return 本コンテキスト
     */
    @Nonnull public UsernameTotpContext setTrustsTotpAuthnSession(final boolean trustsSession) {
        this.trustsTotpAuthnSession = trustsSession;
        return this;
    }
}
