/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.AppConfig;

/**
 * AES暗号化、複合化のためのクラスです.
 * @author SIOS Technology, Inc.
 */
public final class Aes {
    /** ログ出力準備を行います. */
    private static Logger logger = LoggerFactory.getLogger(Aes.class);
    /** bit計算用の定数を定義します. */
    private static final int BIT_NUM = 8;
    /** 暗号化複合化用のブロックビットです. */
    private static final int BLOCK_NUM = 128;
    /** 暗号化factory用の定数を定義します. */
    private static final String FACTORY = "PBKDF2WithHmacSHA1";
    /** getBytesする際の文字コードを定義します. */
    private static final String ENCODE = "UTF-8";

    /**
     * AESのプライベートコンストラクタです. 外部クラスでインスタンスを生成しない.
     */
    private Aes() {
    }

    /**
     * AES256で暗号化します.
     * @param password パスフレーズ
     * @param data 秘密鍵
     * @return Base64.Base64Encode
     * @throws NoSuchAlgorithmException 暗号化アルゴリズムが要求されましたが、利用不可能です.
     * @throws NoSuchPaddingException パディング機能が要求されましたが、利用不可能です.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがあります.
     * @throws InvalidAlgorithmParameterException 無効または不適切なアルゴリズムパラメータです.
     * @throws InvalidKeySpecException 無効な鍵仕様です.
     * @throws IllegalBlockSizeException 暗号のブロックサイズとマッチしません.
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行なえませんでした.
     * @throws InvalidParameterSpecException 無効なパラメータ例外です.
     * @throws UnsupportedEncodingException 文字エンコードが不正です.
     */
    public static byte[] encrypt(String password, String data) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        logger.debug("[BEGIN]: Aes > encrypt()");
        logger.debug("[INPUT] ${password} (String): " + password);
        logger.debug("[INPUT] ${data} (String): " + data);

        // Salt を生成する
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[AppConfig.getSaltLength()];
        random.nextBytes(salt);
        logger.debug("${salt} (byte[]): " + Arrays.toString(salt));
        logger.debug("${salt.length}: " + salt.length);

        // 暗号化されたデータと Salt を合わせた値を返す
        Cipher cipher = createCipher(password, salt, Cipher.ENCRYPT_MODE);

        logger.debug("[ END ]: Aes > encrypt()");
        return merge(cipher.doFinal(data.getBytes(ENCODE)), salt);
    }

    /**
     * AES-256で暗号化されたデータを複合化します。 Base64でエンコードされたdataとパスフレーズを利用して複合化処理を行ないます.
     * @param password パスフレーズ
     * @param data AESで暗号化されたものをさらにBase64で暗号化した秘密鍵
     * @return byte[]
     * @throws NoSuchAlgorithmException 暗号化アルゴリズムが要求されましたが、利用不可能です.
     * @throws NoSuchPaddingException パディング機能が要求されましたが、利用不可能です.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがあります.
     * @throws InvalidKeySpecException 無効な鍵仕様です.
     * @throws IllegalBlockSizeException 暗号のブロックサイズとマッチしません.
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行なえませんでした.
     * @throws InvalidAlgorithmParameterException 無効なアルゴリズムパラメータです.
     * @throws InvalidParameterSpecException 無効なパラメータ例外です.
     */
    public static byte[] decrypt(String password, byte[] data) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidParameterSpecException {
        logger.debug("[BEGIN]: Aes > decrypt()");
        logger.debug("[INPUT] ${password} (String): " + password);
        logger.debug("[INPUT] ${data} (byte[]): " + Arrays.toString(data));

        // 暗号化済みデータ (データ + Salt) から Salt 部を取得する
        byte[] salt = new byte[AppConfig.getSaltLength()];
        for (int i = 0; i < AppConfig.getSaltLength(); i++) {
            salt[i] = data[data.length - AppConfig.getSaltLength() + i];
        }
        logger.debug("${salt} (byte[]): " + Arrays.toString(salt));
        logger.debug("${salt.length}: " + salt.length);

        // 暗号化済みデータ (データ + Salt) からデータ部を取得する
        byte[] rawdata = new byte[data.length - AppConfig.getSaltLength()];
        for (int j = 0; j < data.length - AppConfig.getSaltLength(); j++) {
            rawdata[j] = data[j];
        }
        logger.debug("${rawdata} (byte[]): " + Arrays.toString(rawdata));
        logger.debug("${rawdata.length}: " + rawdata.length);

        // 複合化されたデータを返却する
        Cipher cipher = createCipher(password, salt, Cipher.DECRYPT_MODE);

        logger.debug("[ END ]: Aes > decrypt()");
        return cipher.doFinal(rawdata);
    }

    /**
     * AESの共通ロジックです。 シークレットキーの生成から、暗号化・複合化のモード選択までを行い、 resultCipherを返却します.
     * @param password パスワードフレーズの文字配列
     * @param salt 算出したsalt値のbyte列
     * @param mode 暗号化、複合化のモード選択
     * @return Cipher
     * @throws NoSuchAlgorithmException 暗号化アルゴリズムが要求されましたが、利用不可能です.
     * @throws InvalidKeySpecException 無効な符号化、長さに誤りがあります.
     * @throws NoSuchPaddingException パディング機能が要求されましたが、利用不可能です.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがあります.
     * @throws InvalidAlgorithmParameterException 無効なパラメータです.
     */
    private static Cipher createCipher(String password, byte[] salt, int mode)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        logger.debug("[BEGIN]: Aes > createCipher()");
        logger.debug("[INPUT] ${password} (String): " + password);
        logger.debug("[INPUT] ${salt} (byte[]): " + Arrays.toString(salt));
        logger.debug("[INPUT] ${mode} (int): " + mode);

        logger.debug("${AppConfig.getIterationCount()}: " + AppConfig.getIterationCount());
        logger.debug("${AppConfig.getCipherKeyLength()}: " + AppConfig.getCipherKeyLength());

        // Cipher / SecretKeyFactory オブジェクトを生成する
        Cipher cipher = Cipher.getInstance(AppConfig.getCipherTransformationName());
        SecretKeyFactory fact = SecretKeyFactory.getInstance(FACTORY);

        // 暗号化・復号化時の秘密鍵・IV に利用するバイト配列を PBKDF2-HMAC-SHA1 を用いて生成する
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, AppConfig.getIterationCount(),
                AppConfig.getCipherKeyLength() + BLOCK_NUM);
        byte[] secret = fact.generateSecret(spec).getEncoded();
        logger.debug("${secret} (byte[]): " + Arrays.toString(secret));
        logger.debug("${secret.length}: " + secret.length);

        // 暗号化・復号化に利用する秘密鍵を取得する
        byte[] cipherKey = new byte[(AppConfig.getCipherKeyLength() / BIT_NUM)];
        for (int i = 0; i < cipherKey.length; i++) {
            cipherKey[i] = secret[i];
        }
        logger.debug("${cipherKey} (byte[]): " + Arrays.toString(cipherKey));
        logger.debug("${cipherKey.length}: " + cipherKey.length);

        // 暗号化・復号化に利用する IV を取得する
        byte[] cipherIv = new byte[(BLOCK_NUM / BIT_NUM)];
        for (int i = 0; i < cipherIv.length; i++) {
            cipherIv[i] = secret[secret.length - cipherIv.length + i];
        }
        logger.debug("${cipherIv} (byte[]): " + Arrays.toString(cipherIv));
        logger.debug("${cipherIv.length}: " + cipherIv.length);

        cipher.init(mode, new SecretKeySpec(cipherKey, AppConfig.getCipherArgorithm()),
                new IvParameterSpec(cipherIv));

        logger.debug("[ END ]: Aes > createCipher()");
        return cipher;
    }

    /**
     * byte列同士を結合します.
     * @param resultBase マージのベース
     * @param resultMerge ベースに対してマージするもの
     * @return margeResult
     */
    private static byte[] merge(byte[] resultBase, byte[] resultMerge) {
        logger.debug("[BEGIN]: Aes > merge()");
        logger.debug("[INPUT] ${resultBase} (byte[]): " + Arrays.toString(resultBase));
        logger.debug("[INPUT] ${resultMerge} (byte[]): " + Arrays.toString(resultMerge));

        byte[] mergeResult = new byte[resultBase.length + resultMerge.length];
        for (int i = 0; i < resultBase.length; i++) {
            mergeResult[i] = resultBase[i];
        }
        for (int j = 0; j < resultMerge.length; j++) {
            mergeResult[resultBase.length + j] = resultMerge[j];
        }
        logger.debug("[ END ]: Aes > merge()");
        return mergeResult;
    }
}
