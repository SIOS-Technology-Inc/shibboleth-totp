/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.common.util;

import static org.junit.Assert.*;

import java.security.GeneralSecurityException;
import java.text.MessageFormat;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;

import com.sios.idp.shibboleth.exception.SecretKeyDecryptionException;

/**
 * {@link com.sios.idp.shibboleth.common.util.ExceptionUtil}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class SecretKeyDecrypterTest {

    @Mocked
    final Base64 _base64 = null;
    @Mocked
    final Aes _aes = null;
    @Mocked
    final Base32 _base32 = null;

    /**
     * 001: byte[] decrypt(String encryptedSecretKey, String secretKeyPassPhrase) のテストメソッドです.
     * BASE64デコード、AES復号化、BASE32デコードの順に処理が呼び出されていることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testDecrypt001() throws Exception {
        final String key = "encryptedKey";
        final String pass = "pass";
        final byte[] keyByteArray = new byte[] { -1, -1 };
        final byte[] decryptedKeyByteArray = new byte[] { -11, -11 };
        final byte[] encodedKeyByteArray = new byte[] { -111, -111 };
        new Expectations() {
            {
                Base64.decode(key);
                result = keyByteArray;
                Aes.decrypt(pass, keyByteArray);
                result = decryptedKeyByteArray;
                Base32.decode(new String(decryptedKeyByteArray));
                result = encodedKeyByteArray;
            }
        };
        byte[] actual = SecretKeyDecrypter.decrypt(key, pass);
        assertEquals(new String(encodedKeyByteArray), new String(actual));
    }

    /**
     * 001: byte[] decrypt(String encryptedSecretKey, String secretKeyPassPhrase) のテストメソッドです.
     * AES復号化処理でGeneralSecurityExceptionがthrowされた場合、 SecretKeyDecryptionExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testDecrypt002() throws Exception {
        final String key = "encryptedKey";
        final String pass = "pass";
        final byte[] keyByteArray = new byte[] { -1, -1 };
        new Expectations() {
            {
                Base64.decode(key);
                result = keyByteArray;
                Aes.decrypt(pass, keyByteArray);
                result = new GeneralSecurityException();
            }
        };
        try {
            SecretKeyDecrypter.decrypt(key, pass);
            fail("例外が発生するはず");
        } catch (SecretKeyDecryptionException e) {
            assertEquals(MessageFormat.format("秘密鍵の復号化に失敗しました。 (暗号化秘密鍵：{0}、パスフレーズ：{1})", key, pass), e.getMessage());
        }
    }
}
