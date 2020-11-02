/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * Base32に関する単体試験用クラスです.
 * @author SIOS Technology, Inc.
 */
public class Base32Test {
    /**
     * 001 Base32のエンコード機能が正常動作する事を確認するためのテストです.
     * @throws UnsupportedEncodingException 無効なエンコード設定
     */
    @Test
    public void base32encord() throws UnsupportedEncodingException {
        final String value = "TESTVALUE";
        final byte[] valueByte = value.getBytes("UTF-8");
        String result = null;
        result = Base32.encode(valueByte);
        if (result.equals(null)) {
            fail();
        }
        assertNotNull(result);
    }

    /**
     * 002 Base32のエンコードした値をでコードした際に正常に動作する事を確認するためのテストです.
     * @throws UnsupportedEncodingException 無効なエンコード設定
     */
    @Test
    public void base32EncordDecord() throws UnsupportedEncodingException {
        final String value = "TESTVALUE";
        final byte[] valueByte = value.getBytes("UTF-8");
        String result = null;
        result = Base32.encode(valueByte);
        if (result.equals(null)) {
            fail();
        }
        byte[] resultByte = Base32.decode(result);
        if (resultByte.equals(null)) {
            fail();
        }
        assertEquals(new String(valueByte, "UTF-8"), value);
    }

    /**
     * 003 デコードする際に不正な値が引数と渡されている時、例外が出力される事を確認する.
     */
    @Test(expected = IllegalArgumentException.class)
    public void base32DecordIllegalArg() {
        final String value = "1111111111111";
        Base32.decode(value);
    }

    /**
     * 004 デコードする際に不正な値が引数として渡されている時、例外が出力される事を確認する.
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void base32DecordIllegalAlNum() {
        final String value = "11askjdasoifw121r32hjdfrtp9";
        Base32.decode(value);
    }

    /**
     * 005 デコードする際に不正な値が引数として渡されている時、例外が出力される事を確認する.
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void base32DecordSymbol() {
        final String value = "===---^^^\\\\lll;:]./";
        Base32.decode(value);
    }

    /**
     * 006 エンコードする際に数字のみの場合のみでもエンコードされる事を確認する.
     *
     */
    @Test
    public void base32EncordNum() {
        final Integer value = 1111111111;
        int arraysize = Integer.SIZE / Byte.SIZE;
        ByteBuffer buf = ByteBuffer.allocate(arraysize);
        Base32.encode(buf.putInt(value).array());
    }

    /**
     * 007 エンコードする際にアルファベットのみでもエンコードされる事を確認する.
     *
     */
    @Test
    public void base32EncordAl() {
        final String value = "abcdefghijklmnopqrstuvwxyz";
        Base32.encode(value.getBytes());
    }

    /**
     * 008 エンコードする際に記号のみでもエンコードされる事を確認する.
     *
     */
    @Test
    public void base32EncordSymbol() {
        final String value = "!#$%&()-^\\@[;:],";
        Base32.encode(value.getBytes());
    }
}
