/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.totp;

import java.security.GeneralSecurityException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.common.util.SecretKeyDecrypter;
import com.sios.idp.shibboleth.datasource.dao.Dao;
import com.sios.idp.shibboleth.datasource.dao.DaoFactory;
import com.sios.idp.shibboleth.dto.SearchResult;
import com.sios.idp.shibboleth.exception.DaoInstantiationException;
import com.sios.idp.shibboleth.exception.DataAccessException;
import com.sios.idp.shibboleth.exception.InvalidUserDataException;
import com.sios.idp.shibboleth.exception.SecretKeyDecryptionException;
import com.sios.idp.shibboleth.exception.TotpGenerationException;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;

/**
 * {@link com.sios.idp.shibboleth.authn.totp.TotpProvider}の実装クラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpProviderImpl implements TotpProvider {

    /** Class logger. */
    private final Logger _logger = LoggerFactory.getLogger(this.getClass());

    /** {@inheritDoc} */
    @Override
    public Totp getTotp(String userName) throws TotpGenerationException {

        // タイムカウント取得
        TotpCounter counter = new TotpCounter(AppConfig.getTimeStepSec());
        long timeCounter = counter.getTimeCount();

        // ユーザ固有ID取得
        SearchResult userInfoMap = getUserInfo(userName);
        String attributeName = AppConfig.getImmutableUserIdAttributeName();
        String immutableUserId = userInfoMap.getValue(attributeName);
        if (immutableUserId == null || immutableUserId.length() == 0) {
            throw new InvalidUserDataException(MessageFormat.format(
                    "ユーザ名 ：{0}のユーザ固有ID (データ属性名：{1})が取得できませんでした。", userName, attributeName));
        }

        // TOTPキャッシュの存在チェック
        TotpCache cache = TotpCache.getInstance();
        if (cache.exists(immutableUserId, timeCounter, AppConfig.getAllowedTimeCountOffset())) {
            // キャッシュ存在時は生成済みTOTPを返却
            Totp cachedTotp = cache.get(immutableUserId, timeCounter);
            _logger.debug("TOTPをキャッシュから取得しました。 (ユーザ固有ID：{}, タイムカウント：{}, TOTP：{})",
                    immutableUserId, cachedTotp.getTimeCounter(), cachedTotp.getTotp());
            return cachedTotp;
        }

        // 暗号化秘密鍵の取得
        String encryptedSecretKey = getUserSecretKey(userInfoMap);

        // 秘密鍵の復号化
        byte[] keyBytes = decryptSecretKey(encryptedSecretKey);

        // TOTP生成
        Totp totp =  generateTotp(
                immutableUserId, AppConfig.getTotpLength(), timeCounter, keyBytes);
        _logger.debug("TOTPを生成しました。 (ユーザ固有ID：{}, タイムカウント：{}, TOTP：{})",
                immutableUserId, totp.getTimeCounter(), totp.getTotp());
        cache.add(immutableUserId, totp);

        // 現在のタイムカウント以前のTOTPをキャッシュ
        cachePreviousTotp(immutableUserId, timeCounter, keyBytes);

        return totp;
    }

    /**
     * ユーザの秘密鍵を取得します.
     * @param userInfoMap ユーザ情報
     * @return 暗号化された秘密鍵
     * @throws TotpGenerationException ユーザ情報が複数件存在した場合、ユーザ情報の取得に失敗、
     *   及び取得結果が0件の場合、秘密鍵が取得できない場合
     */
    private String getUserSecretKey(SearchResult userInfoMap) throws TotpGenerationException {

        _logger.debug("秘密鍵の取得を開始します。");
        String secretKey = userInfoMap.getValue(AppConfig.getSecretKeyAttributeName());
        if (secretKey == null || secretKey.isEmpty()) {
            throw new TotpGenerationException("秘密鍵の取得に失敗しました。");
        }
        _logger.debug("秘密鍵の取得に成功しました。");
        return secretKey;
    }

    /**
     * ユーザ名を検索条件にユーザ情報を取得します.
     * @param userName ユーザ名
     * @return ユーザ情報
     * @throws TotpGenerationException ユーザ情報が不正な場合
     */
    private SearchResult getUserInfo(String userName) throws TotpGenerationException {

        _logger.info("ユーザ名：{}のユーザ情報の取得を開始します。", userName);
        Dao dao = null;
        try {
            dao = DaoFactory.getInstance().createInstance();
        } catch (DaoInstantiationException e) {
            handleException(e, "DAOインタフェース {0} の実装クラスのインスタンス生成に失敗しました", Dao.class.getName());
        }
        SearchResult result = null;
        try {
            result = dao.getUser(userName);
        } catch (UserDuplicatedException e) {
            handleException(e, "ユーザ名：{0}のユーザ情報が複数件存在します。", userName);
        } catch (DataAccessException e) {
            handleException(e, "ユーザ名：{0}のユーザ情報取得時にデータアクセスエラーが発生しました。", userName);
        } catch (UnexpectedException e) {
            handleException(e, "ユーザ名：{0}のユーザ情報取得時に予期せぬエラーが発生しました。", userName);
        }

        if (result == null) {
            String msg = MessageFormat.format("ユーザ名：{0}のユーザ情報が取得できませんでした。", userName);
            _logger.error(msg);
            throw new TotpGenerationException(msg);
        }
        _logger.info("ユーザ名：{}のユーザ情報の取得に成功しました。", userName);
        return result;
    }

    /**
     * 秘密鍵を復号化します.<br/>
     * 以下の手順で復号化します。
     * <ol>
     * <li>BASE64デコード</li>
     * <li>AES-256-CBC復号化</li>
     * <li>BASE32デコード</li>
     * </ol>
     * @param encryptedSecretKey 暗号化秘密鍵
     * @return 復号化された秘密鍵のバイト配列
     * @throws TotpGenerationException 秘密鍵の復号化に失敗した場合、未サポートのエンコーディングの場合
     * @throws SecretKeyDecryptionException
     */
    private byte[] decryptSecretKey(String encryptedSecretKey) throws TotpGenerationException
    {
        _logger.debug("秘密鍵の復号化を開始します。");
        byte[] key = null;
        try {
            key = SecretKeyDecrypter.decrypt(
                    encryptedSecretKey, AppConfig.getSecretKeyPassPhrase());
        } catch (SecretKeyDecryptionException e) {
            handleException(e, "秘密鍵の復号化に失敗しました。");
        }

        if (key == null || key.length == 0) {
            throw new TotpGenerationException("秘密鍵の復号化に失敗しました。");
        }
        _logger.debug("秘密鍵の復号化に成功しました。");
        return key;
    }

    /**
     * TOTPを生成します.
     * @param immutableUserId ユーザ固有ID
     * @param totpLength TOTP桁数
     * @param timeCounter タイムカウンタ
     * @param keyBytes TOTP生成に使用する復号化済み秘密鍵
     * @return TOTPオブジェクト
     * @throws TotpGenerationException TOTP生成に失敗した場合
     */
    private Totp generateTotp(
            String immutableUserId, int totpLength, long timeCounter, byte[] keyBytes)
            throws TotpGenerationException {

        TotpGenerator totpGenerator = new TotpGenerator(keyBytes, timeCounter, totpLength);
        String totpValue = null;
        try {
            totpValue = totpGenerator.generateTotp();
        } catch (GeneralSecurityException e) {
            _logger.error("TOTPの生成に失敗しました。", e);
            throw new TotpGenerationException(e);
        }
        Totp totp = new Totp(immutableUserId, timeCounter, totpValue);
        return totp;
    }

    /**
     * 指定タイムカウントから任意オフセット値(※)を差し引いたタイムカウントまでのTOTPをキャッシュします.
     * 既にキャッシュされている場合は何もしません。<br/>
     * (※)  {@link com.sios.idp.shibboleth.common.AppConfig｝のallowedTimeCountOffset値に従います。
     * @param immutableUserId ユーザ固有ID
     * @param timeCounter タイムカウント
     * @param secretKey 秘密鍵
     * @throws TotpGenerationException TOTP生成に失敗した場合
     */
    private void cachePreviousTotp(String immutableUserId, long timeCounter, byte[] secretKey)
            throws TotpGenerationException {

        int cntOffset = AppConfig.getAllowedTimeCountOffset();
        TotpCache cache = TotpCache.getInstance();
        for (int i = 0; i <= cntOffset; i++) {
            long timeCnt = timeCounter - i;
            if (!cache.exists(immutableUserId, timeCnt)) {
               Totp totp = generateTotp(
                       immutableUserId, AppConfig.getTotpLength(), timeCnt, secretKey);
               cache.add(immutableUserId, totp);
            }
        }
    }

    /**
     * 例外を処理します.
     * @param e 例外オブジェクト
     * @param message メッセージ
     * @param messageArgs メッセージ埋め込み文字列
     * @throws TotpGenerationException TOTP生成に失敗した場合
     */
    private void handleException(Exception e, String message, Object... messageArgs)
            throws TotpGenerationException {
        String msg = MessageFormat.format(message, messageArgs);
        _logger.error(msg);
        throw new TotpGenerationException(msg, e);
    }
}
