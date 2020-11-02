/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import java.security.GeneralSecurityException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.exception.SecretKeyDecryptionException;

/**
 * 秘密鍵の復号化クラスです.
 * @author SIOS Technology, Inc.
 */
public final class SecretKeyDecrypter {

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretKeyDecrypter.class);

    /** privateコンストラクタ. */
    private SecretKeyDecrypter() { }

    /**
     * 秘密鍵を復号化します.<br>
     * 以下の手順で復号化します。
     * <ol>
     * <li>BASE64デコード</li>
     * <li>AES-256-CBC復号化</li>
     * <li>BASE32デコード</li>
     * </ol>
     * @param encryptedSecretKey 暗号化秘密鍵
     * @param secretKeyPassPhrase 秘密鍵パスフレーズ
     * @return 復号化された秘密鍵のバイト配列
     * @throws SecretKeyDecryptionException 秘密鍵の復号化に失敗した場合、未サポートのエンコーディングの場合
     */
    public static byte[] decrypt(String encryptedSecretKey, String secretKeyPassPhrase)
            throws SecretKeyDecryptionException {

        byte[] decodedKey = Base64.decode(encryptedSecretKey);
        byte[] dectyptedKey = null;
        try {
            dectyptedKey = Aes.decrypt(secretKeyPassPhrase, decodedKey);
        } catch (GeneralSecurityException e) {
            handleException(e, "秘密鍵の復号化に失敗しました。 (暗号化秘密鍵：{0}、パスフレーズ：{1})",
                    encryptedSecretKey, secretKeyPassPhrase);
        }
        return Base32.decode(new String(dectyptedKey));
    }

    /**
     * 例外を処理します.
     * @param e 例外オブジェクト
     * @param message メッセージ
     * @param messageArgs メッセージ埋め込み文字列
     * @throws SecretKeyDecryptionException 秘密鍵の復号化に失敗した場合
     */
    private static void handleException(Exception e, String message, Object... messageArgs)
            throws SecretKeyDecryptionException {
        String msg = MessageFormat.format(message, messageArgs);
        LOGGER.error(msg);
        throw new SecretKeyDecryptionException(msg, e);
    }
}
