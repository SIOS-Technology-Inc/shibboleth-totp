/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.util.TotpAuthnSessionIdGenerator;
import com.sios.idp.shibboleth.exception.TotpAuthnSessionIdGenerationException;

/**
 * TOTP認証セッションの整合性を確認するためのクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpAuthnSessionChecker {

    /** ログ出力準備を行います. */
    @Nonnull
    private final Logger logger = LoggerFactory.getLogger(TotpAuthnSessionChecker.class);

    /** Cookie読込に利用するHTTPリクエストです. */
    @Nonnull
    private HttpServletRequest httpServletRequest;

    /** コンストラクタです. */
    public TotpAuthnSessionChecker() {
    }

    /**
     * HTTPリクエストの設定.
     * @param request HTTPリクエスト
     */
    public void setHttpServletRequest(@Nonnull final HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    /**
     * TOTP認証セッションの整合性を確認します。
     * @param username ユーザー名
     * @return TOTP認証セッションが正当=true, TOTP認証セッションが不正=false
     */
    public boolean isValidTotpAuthnSession(@Nullable final String username) {
        logger.info("TOTP認証セッションの整合性チェックを開始します。");
        logger.debug("[INPUT] ユーザー名 : {}", username);

        final Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            logger.info("HTTPリクエストにCookieが設定されていません。");
            return false;
        }

        String actualSessionId = null;
        String issueDateString = null;
        for (final Cookie cookie : cookies) {
            final String cookieName = cookie.getName();
            if (AppConfig.getTotpAuthnSessionIdCookieName().equals(cookieName)) {
                actualSessionId = cookie.getValue();
                logger.debug("TOTP認証セッションID : {}", actualSessionId);
            } else if (AppConfig.getTotpAuthnSessionIssueDateCookieName().equals(cookieName)) {
                issueDateString = cookie.getValue();
                logger.debug("処理日時 : {}", issueDateString);
            }
        }
        if (actualSessionId == null && issueDateString == null) {
            logger.info("TOTP認証セッションがCookieに保存されていません。");
            return false;
        } else if (actualSessionId == null) {
            logger.warn("TOTP認証セッションIDがCookieに保存されていないため、セッションを検証できません。");
            return false;
        } else if (issueDateString == null) {
            logger.warn("処理日時がCookieに保存されていないため、セッションを検証できません。");
            return false;
        }

        long issueDate;
        try {
            issueDate = Long.parseLong(issueDateString);
        } catch (NumberFormatException e) {
            logger.warn("Cookieに保存されている処理日時が不正です。");
            return false;
        }
        if (System.currentTimeMillis() / 1000L > issueDate
                + AppConfig.getTotpAuthnSessionExpirationSec()) {
            logger.warn("TOTP認証セッションの有効期限が切れています。");
            return false;
        }

        String expectedSessionId;
        try {
            expectedSessionId = TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(
                    username, issueDateString);
            logger.debug("検証用TOTP認証セッションID : {}", expectedSessionId);
        } catch (TotpAuthnSessionIdGenerationException e) {
            logger.error("検証用TOTP認証セッションの生成に失敗しました。");
            return false;
        }

        if (actualSessionId.equals(expectedSessionId)) {
            logger.info("TOTP認証セッションは正当な値です。");
            return true;
        } else {
            logger.warn("TOTP認証セッションは不正な値です。");
            return false;
        }
    }
}
