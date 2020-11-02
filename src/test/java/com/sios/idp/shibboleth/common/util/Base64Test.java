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
 * Base64クラスの単体テスト用クラスです..
 * @author SIOS Technology, Inc.
 */
public class Base64Test {
    /**
     * 001 Base64のエンコード機能が正常動作する事を確認するためのテストです.
     * @throws UnsupportedEncodingException 無効なエンコード設定
     */
    @Test
    public void base64encord() throws UnsupportedEncodingException {
        final String value = "TESTVALUE";
        final byte[] valueByte = value.getBytes("UTF-8");
        String result = null;
        result = Base64.encode(valueByte);
        if (result.equals(null)) {
            fail();
        }
        assertNotNull(result);
    }

    /**
     * 002 Base64のエンコードした値をでコードした際に正常に動作する事を確認するためのテストです.
     * @throws UnsupportedEncodingException 無効なエンコード設定
     */
    @Test
    public void base64EncordDecord() throws UnsupportedEncodingException {
        final String value = "TESTVALUE";
        final byte[] valueByte = value.getBytes("UTF-8");
        String result = null;
        result = Base64.encode(valueByte);
        if (result.equals(null)) {
            fail();
        }
        byte[] resultByte = Base64.decode(result);
        if (resultByte.equals(null)) {
            fail();
        }
        assertEquals(new String(valueByte, "UTF-8"), value);
    }

    /**
     * 003 デコードする際に不正な値が引数と渡されている時、例外が出力される事を確認する.
     */
    @Test(expected = IllegalArgumentException.class)
    public void base64DecordIllegalArg() {
        final String value = "]]]]]]]]]]]]]]]";
        Base64.decode(value);
    }

    /**
     * 004 デコードする際に不正な値が引数として渡されている時、例外が出力される事を確認する.
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void base64DecordSymbol() {
        final String value = "---^^^\\\\lll;:]./";
        Base64.decode(value);
    }

    /**
     * 005 エンコードする際に数字のみの場合のみでもエンコードされる事を確認する.
     *
     */
    @Test
    public void base64EncordNum() {
        final Integer value = 1111111111;
        int arraysize = Integer.SIZE / Byte.SIZE;
        ByteBuffer buf = ByteBuffer.allocate(arraysize);
        Base64.encode(buf.putInt(value).array());
    }

    /**
     * 006 エンコードする際にアルファベットのみでもエンコードされる事を確認する.
     *
     */
    @Test
    public void base64EncordAl() {
        final String value = "abcdefghijklmnopqrstuvwxyz";
        Base64.encode(value.getBytes());
    }

    /**
     * 007 エンコードする際に記号のみでもエンコードされる事を確認する.
     *
     */
    @Test
    public void base64EncordSymbol() {
        final String value = "!#$%&()-^\\@[;:],";
        Base64.encode(value.getBytes());
    }
}
