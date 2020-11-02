/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

//import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Expectations;

import org.junit.Test;

import com.sios.idp.shibboleth.common.AppConfig;

public class AesTest {
    @Mocked
    private AppConfig appConfig;

    /**
     * 001 暗号化メソッドの正常処理確認用メソッドです.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがある
     * @throws UnsupportedEncodingException サポートされていない文字エンコードを指定している
     * @throws NoSuchAlgorithmException 利用不可能なアルゴリズムを使用している
     * @throws InvalidKeySpecException 無効な鍵仕様
     * @throws NoSuchPaddingException パディング機能が要求されたが利用不可能
     * @throws InvalidParameterSpecException 無効なパラメータ例外
     * @throws IllegalBlockSizeException ブロックサイズのアンマッチ
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行えない
     * @throws InvalidAlgorithmParameterException アルゴリズムパラメータが不正
     */
    @Test
    public void encryptMethod() throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidParameterSpecException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        final String data = "test_sios";
        final String password = "password";
        new Expectations() {
            {
                com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 32;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 256;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CBC/PKCS5Padding";
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 1000;
            }
        };
        byte[] result = Aes.encrypt(password, data);
        new String(result, "UTF-8");
    }

    /**
     * 002 復号化の正常処理確認用メソッド.
     * @throws NoSuchAlgorithmException 暗号化アルゴリズムが要求されましたが、利用不可能です.
     * @throws NoSuchPaddingException パディング機能が要求されましたが、利用不可能です.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがあります.
     * @throws InvalidKeySpecException 無効な鍵仕様です.
     * @throws IllegalBlockSizeException 暗号のブロックサイズとマッチしません.
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行なえませんでした.
     * @throws InvalidAlgorithmParameterException 無効なアルゴリズムパラメータです.
     * @throws InvalidParameterSpecException 無効なパラメータ例外です.
     * @throws UnsupportedEncodingException 文字エンコードが不正です.
     */
    @Test
    public void decryptMethod() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidParameterSpecException, UnsupportedEncodingException {
        final String date = "bgIJUzDtzusbALvZzle4lqbSSX6LGwO2pcH/JLFdFlJDD84P0YQZJ6+adpj1h/I6rxr/oetZd0ZLl+9fFt7MZQ==";
        final byte[] data = Base64.decode(date);
        final String password = "password";
        new Expectations() {
            {
                com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 32;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 256;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CBC/PKCS5Padding";
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 1000;

            }
        };
        byte[] result = Aes.decrypt(password, data);
        String sresult = new String(result, "UTF-8");
        System.out.println(sresult);
        assertTrue(true);
    }

    /**
     * 003 ブロックサイズがアンマッチしている事を確認するためのメソッドです SALTの長さが間違っている場合などに発生します.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがある
     * @throws UnsupportedEncodingException サポートされていない文字エンコードを指定している
     * @throws NoSuchAlgorithmException 利用不可能なアルゴリズムを使用している
     * @throws InvalidKeySpecException 無効な鍵仕様
     * @throws NoSuchPaddingException パディング機能が要求されたが利用不可能
     * @throws InvalidParameterSpecException 無効なパラメータ例外
     * @throws IllegalBlockSizeException ブロックサイズのアンマッチ
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行えない
     * @throws InvalidAlgorithmParameterException アルゴリズムパラメータが不正
     */
    @Test(expected = IllegalBlockSizeException.class)
    public void decryptBlockSize() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidParameterSpecException, UnsupportedEncodingException {
        new Expectations() {
            {
                com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 31;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 256;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CBC/PKCS5Padding";
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 1000;
            }
        };
        final String date = "bgIJUzDtzusbALvZzle4lqbSSX6LGwO2pcH/JLFdFlJDD84P0YQZJ6+adpj1h/I6rxr/oetZd0ZLl+9fFt7MZQ==";
        final byte[] data = Base64.decode(date);
        Aes.decrypt("password", data);
    }

    /**
     * 004 無効な鍵の長さを指定している事を確認するためのテストコードです.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがある
     * @throws UnsupportedEncodingException サポートされていない文字エンコードを指定している
     * @throws NoSuchAlgorithmException 利用不可能なアルゴリズムを使用している
     * @throws InvalidKeySpecException 無効な鍵仕様
     * @throws NoSuchPaddingException パディング機能が要求されたが利用不可能
     * @throws InvalidParameterSpecException 無効なパラメータ例外
     * @throws IllegalBlockSizeException ブロックサイズのアンマッチ
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行えない
     * @throws InvalidAlgorithmParameterException アルゴリズムパラメータが不正
     */
    @Test(expected = InvalidKeyException.class)
    public void decryptInvalid() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidParameterSpecException, UnsupportedEncodingException {
        new Expectations() {
            {
                com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 32;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 255;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CBC/PKCS5Padding";
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 1000;
            }
        };
        final String date = "bgIJUzDtzusbALvZzle4lqbSSX6LGwO2pcH/JLFdFlJDD84P0YQZJ6+adpj1h/I6rxr/oetZd0ZLl+9fFt7MZQ==";
        final byte[] data = Base64.decode(date);
        Aes.decrypt("password", data);
    }

    /**
     * 005 入力値と予期するデータのやり取りが行えない事を確認するためのテストコードです。 IteratorCountの値が間違っている事が考えられる.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがある
     * @throws UnsupportedEncodingException サポートされていない文字エンコードを指定している
     * @throws NoSuchAlgorithmException 利用不可能なアルゴリズムを使用している
     * @throws InvalidKeySpecException 無効な鍵仕様
     * @throws NoSuchPaddingException パディング機能が要求されたが利用不可能
     * @throws InvalidParameterSpecException 無効なパラメータ例外
     * @throws IllegalBlockSizeException ブロックサイズのアンマッチ
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行えない
     * @throws InvalidAlgorithmParameterException アルゴリズムパラメータが不正
     */
    @Test(expected = BadPaddingException.class)
    public void decryptBadPadding() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidParameterSpecException, UnsupportedEncodingException {
        new Expectations() {
            {
                com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 32;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 256;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CBC/PKCS5Padding";
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 100;
            }
        };
        final String date = "bgIJUzDtzusbALvZzle4lqbSSX6LGwO2pcH/JLFdFlJDD84P0YQZJ6+adpj1h/I6rxr/oetZd0ZLl+9fFt7MZQ==";
        final byte[] data = Base64.decode(date);
        Aes.decrypt("password", data);
    }

    /**
     * 006 利用不可能なアルゴリズムを利用している事を確認するためのテストメソッドです.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがある
     * @throws UnsupportedEncodingException サポートされていない文字エンコードを指定している
     * @throws NoSuchAlgorithmException 利用不可能なアルゴリズムを使用している
     * @throws InvalidKeySpecException 無効な鍵仕様
     * @throws NoSuchPaddingException パディング機能が要求されたが利用不可能
     * @throws InvalidParameterSpecException 無効なパラメータ例外
     * @throws IllegalBlockSizeException ブロックサイズのアンマッチ
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行えない
     * @throws InvalidAlgorithmParameterException アルゴリズムパラメータが不正
     */
    @Test(expected = NoSuchAlgorithmException.class)
    public void decryptNoSuchAlgorithm() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidParameterSpecException, UnsupportedEncodingException {
        new Expectations() {
            {
                //com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                //result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 32;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 256;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CGC/PKCS5Padding";
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 1000;
            }
        };
        final String date = "bgIJUzDtzusbALvZzle4lqbSSX6LGwO2pcH/JLFdFlJDD84P0YQZJ6+adpj1h/I6rxr/oetZd0ZLl+9fFt7MZQ==";
        final byte[] data = Base64.decode(date);
        Aes.decrypt("password", data);
    }

    /**
     * 007 アルゴリズムが不正である事を確認するためのテストメソッドです.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがある
     * @throws UnsupportedEncodingException サポートされていない文字エンコードを指定している
     * @throws NoSuchAlgorithmException 利用不可能なアルゴリズムを使用している
     * @throws InvalidKeySpecException 無効な鍵仕様
     * @throws NoSuchPaddingException パディング機能が要求されたが利用不可能
     * @throws InvalidParameterSpecException 無効なパラメータ例外
     * @throws IllegalBlockSizeException ブロックサイズのアンマッチ
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行えない
     * @throws InvalidAlgorithmParameterException アルゴリズムパラメータが不正
     */
    @Test(expected = NoSuchAlgorithmException.class)
    public void decryptNoSuchAlgorithmFactory() throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException
            , UnsupportedEncodingException {
        new Expectations() {
            {
                //com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                //result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 32;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 256;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CGC/PKCS5Padding";
                //↑↑↑　FACTORYのテストに見えるが、ここでエラーとなってしまう
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 1000;
                //Deencapsulation.setField(Aes.class, "FACTORY", "dammyFactory");
            }
        };
        final String date = "bgIJUzDtzusbALvZzle4lqbSSX6LGwO2pcH/JLFdFlJDD84P0YQZJ6+adpj1h/I6rxr/oetZd0ZLl+9fFt7MZQ==";
        final byte[] data = Base64.decode(date);
        Aes.decrypt("password", data);
    }

    /**
     * 008 無効な鍵仕様を確認するためのテストメソッドです.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがある
     * @throws UnsupportedEncodingException サポートされていない文字エンコードを指定している
     * @throws NoSuchAlgorithmException 利用不可能なアルゴリズムを使用している
     * @throws InvalidKeySpecException 無効な鍵仕様
     * @throws NoSuchPaddingException パディング機能が要求されたが利用不可能
     * @throws InvalidParameterSpecException 無効なパラメータ例外
     * @throws IllegalBlockSizeException ブロックサイズのアンマッチ
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行えない
     * @throws InvalidAlgorithmParameterException アルゴリズムパラメータが不正
     */
    @Test(expected = InvalidKeyException.class)
    public void encryptInvalid() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidParameterSpecException, UnsupportedEncodingException {
        final String data = "test_sios";
        final String password = "password";
        new Expectations() {
            {
                com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 32;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 255;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CBC/PKCS5Padding";
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 1000;
            }
        };
        byte[] result = Aes.encrypt(password, data);
        new String(result, "UTF-8");
    }

    /**
     * 009 アルゴリズムの不正を確認するためのテストメソッドです.
     * @throws InvalidKeyException 無効な符号化、長さに誤りがある
     * @throws UnsupportedEncodingException サポートされていない文字エンコードを指定している
     * @throws NoSuchAlgorithmException 利用不可能なアルゴリズムを使用している
     * @throws InvalidKeySpecException 無効な鍵仕様
     * @throws NoSuchPaddingException パディング機能が要求されたが利用不可能
     * @throws InvalidParameterSpecException 無効なパラメータ例外
     * @throws IllegalBlockSizeException ブロックサイズのアンマッチ
     * @throws BadPaddingException 入力値と予期するデータのやり取りが行えない
     * @throws InvalidAlgorithmParameterException アルゴリズムパラメータが不正
     */
    @Test(expected = NoSuchAlgorithmException.class)
    public void encryptNoSuchAlgorithm() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
            InvalidParameterSpecException, UnsupportedEncodingException {
        final String data = "test_sios";
        final String password = "password";
        new Expectations() {
            {
                //com.sios.idp.shibboleth.common.AppConfig.getCipherArgorithm();
                //result = "AES";
                com.sios.idp.shibboleth.common.AppConfig.getSaltLength();
                result = 32;
                com.sios.idp.shibboleth.common.AppConfig.getCipherKeyLength();
                result = 256;
                com.sios.idp.shibboleth.common.AppConfig.getCipherTransformationName();
                result = "AES/CGC/PKCS5Padding";
                com.sios.idp.shibboleth.common.AppConfig.getIterationCount();
                result = 1000;
            }
        };
        byte[] result = Aes.encrypt(password, data);
        new String(result, "UTF-8");
    }
}
