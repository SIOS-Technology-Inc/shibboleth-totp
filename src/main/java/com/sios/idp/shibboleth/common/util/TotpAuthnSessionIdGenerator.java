/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.datasource.dao.Dao;
import com.sios.idp.shibboleth.datasource.dao.DaoFactory;
import com.sios.idp.shibboleth.dto.SearchResult;
import com.sios.idp.shibboleth.exception.DaoInstantiationException;
import com.sios.idp.shibboleth.exception.DataAccessException;
import com.sios.idp.shibboleth.exception.TotpAuthnSessionIdGenerationException;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UnrecoverableException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;

/**
 * TOTP認証セッションIDを生成するためのクラスです.
 * @author SIOS Technology, Inc.
 */
public final class TotpAuthnSessionIdGenerator {

    /** ログ出力準備を行います. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TotpAuthnSessionIdGenerator.class);

    /**
     * TotpAuthnSessionIdGeneratorプライベートコンストラクタです.
     */
    private TotpAuthnSessionIdGenerator() {
    }

    /**
     * TOTP認証セッションIDを生成します.
     * @param username ユーザー名
     * @param issueDate 処理日時
     * @return TOTP認証セッションID
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッションID生成に失敗した場合
     */
    public static String generateTotpAuthnSessionId(String username, String issueDate)
            throws TotpAuthnSessionIdGenerationException {
        LOGGER.debug("[INPUT] ユーザー名 : {}", username);
        LOGGER.debug("[INPUT] 処理日時 : {}", issueDate);

        String serverSalt = AppConfig.getTotpAuthnSessionIdSalt();
        LOGGER.debug("[CONFIG] ソルト文字列 : {}", serverSalt);

        LOGGER.debug("ユーザーのTOTP秘密鍵の取得を開始します。");
        String encryptedSecretKey = null;
        try {
            encryptedSecretKey = getEncryptedSecretKey(username);
        } catch (UnrecoverableException e) {
            handleException(e, "ユーザーのTOTP秘密鍵の取得に失敗しました。");
        }
        LOGGER.debug("ユーザーのTOTP秘密鍵の取得に成功しました。");
        LOGGER.debug("ユーザーTOTP秘密鍵 : {}", encryptedSecretKey);

        StringBuilder sb = new StringBuilder();
        sb.append(username);
        sb.append(issueDate);
        sb.append(encryptedSecretKey);
        sb.append(serverSalt);

        LOGGER.debug("TOTP認証セッション生成を開始します。");
        byte[] hashedString = null;
        try {
            hashedString = Sha256.hash(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            handleException(e, "TOTP認証セッション生成エラー : {0}アルゴリズムが利用できません。", Sha256.ALGORITHM);
        } catch (UnsupportedEncodingException e) {
            handleException(e, "TOTP認証セッション生成エラー : {0}エンコードが利用できません。", Sha256.ENCODE);
        }
        String encodedString = Base64.encode(hashedString);
        LOGGER.debug("TOTP認証セッション生成に成功しました。");
        LOGGER.debug("[OUTPUT] TOTP認証セッションID : {}", encodedString);

        return encodedString;
    }

    /**
     * LDAPから暗号化されたTOTP秘密鍵を取得します.
     * @param username ユーザ名
     * @return 暗号化されたTOTP秘密鍵
     * @throws DataAccessException ユーザ情報データソースへのアクセスエラーが発生した場合
     * @throws UserDuplicatedException 1つのユーザIDに対して検索結果が複数件だった場合
     * @throws DaoInstantiationException DAOのインスタンス生成に失敗した場合
     * @throws UnexpectedException ユーザ情報データソースアクセス時に予期せぬエラーが発生した場合
     * @throws TotpAuthnSessionIdGenerationException ユーザまたはユーザのTOTP秘密鍵が見つからなかった場合
     */
    private static String getEncryptedSecretKey(String username)
            throws DaoInstantiationException, DataAccessException, UserDuplicatedException,
            UnexpectedException, TotpAuthnSessionIdGenerationException {
        Dao dao = DaoFactory.getInstance().createInstance();
        SearchResult result = dao.getUser(username);
        if (result == null) {
            handleException(null, "ユーザー名{0}の検索結果が0件です。", username);
        }

        String encryptedSecretKey = result.getValue(AppConfig.getSecretKeyAttributeName());
        if (encryptedSecretKey == null) {
            handleException(null, "ユーザー{0}にTOTP秘密鍵が設定されていません。", username);
        }

        return encryptedSecretKey;
    }

    /**
     * 例外を処理します.
     * @param e 例外オブジェクト
     * @param message メッセージ
     * @param messageArgs メッセージ埋め込み文字列
     * @throws TotpAuthnSessionIdGenerationException TOTP認証セッション生成エラーを示す例外
     */
    private static void handleException(Exception e, String message, Object... messageArgs)
            throws TotpAuthnSessionIdGenerationException {
        String msg = MessageFormat.format(message, messageArgs);
        LOGGER.error(msg);
        throw new TotpAuthnSessionIdGenerationException(msg, e);
    }
}
