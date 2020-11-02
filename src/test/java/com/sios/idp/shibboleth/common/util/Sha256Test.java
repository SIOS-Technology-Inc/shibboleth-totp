/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import static org.junit.Assert.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

/**
 * {@link com.sios.idp.shibboleth.common.util.Sha256}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class Sha256Test {

    /**
     * 正常に文字列をハッシュ化できることをテストします.
     */
    @Test
    public void hashNormal() {
        final String value = "TESTVALUE";
        final byte[] expected = { (byte) 0xA3, (byte) 0x41, (byte) 0xC7, (byte) 0xEE,
                (byte) 0x9D, (byte) 0x09, (byte) 0xD8, (byte) 0x93,
                (byte) 0xDC, (byte) 0x27, (byte) 0x13, (byte) 0x71,
                (byte) 0x79, (byte) 0xA4, (byte) 0x9B, (byte) 0xBD,
                (byte) 0xCA, (byte) 0x91, (byte) 0xDE, (byte) 0x7A,
                (byte) 0xD4, (byte) 0x2A, (byte) 0x10, (byte) 0x3E,
                (byte) 0xAE, (byte) 0x02, (byte) 0x4E, (byte) 0xAA,
                (byte) 0x83, (byte) 0x5F, (byte) 0xD0, (byte) 0x44 };
        byte[] actual;
        try {
            actual = Sha256.hash(value);
            assertTrue(Arrays.equals(expected, actual));
        } catch (Exception e) {
            fail("例外が発生したためテストNG");
        }
    }

    /**
     * アルゴリズムが利用できない場合に正しい例外がthrowされることをテストします.
     * @throws NoSuchAlgorithmException アルゴリズムが利用できないことを示す例外
     */
    @Test
    public void hashNoSuchAlgorithm(@Mocked MessageDigest messageDigest) throws NoSuchAlgorithmException {
        final String value = "TESTVALUE";

        //new Expectations(MessageDigest.class) {
        new Expectations() {
            {
                MessageDigest.getInstance(anyString);
                result = new NoSuchAlgorithmException();
            }
        };

        try {
            Sha256.hash(value);
            fail("例外が発生しないためテストNG");
        } catch (Exception e) {
            assertTrue(e instanceof NoSuchAlgorithmException);
        }
    }

    /*
     * 文字コードが利用できない場合に正しい例外がthrowされることをテストする予定だったが、
     * どうしてもStringのモックインスタンスが作成できないためテスト対象外とする.
     */

}
