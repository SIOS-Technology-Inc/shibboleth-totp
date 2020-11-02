/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.authn.totp;

import static org.junit.Assert.*;

import org.junit.Test;

//import com.sios.idp.shibboleth.authn.totp.Totp;

/**
 * {@link com.sios.idp.shibboleth.authn.totp.Totp}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpTest {

    /**
     * 001: boolean equals() のテストメソッドです. ユーザ固有ID、タイムカウンタ、ワンタイムパスワード、認証済みフラグの値の
     * 全てが同一の場合はtrueが返却されることをテストする
     */
    @Test
    public void testEquals() {
        Totp t0_0 = new Totp("001", 0L, "0");
        Totp t0_0_2 = new Totp("001", 0L, "0");
        Totp t0_1 = new Totp("001", 0L, "1");
        Totp t1_1 = new Totp("001", 1L, "1");
        Totp t0_blank = new Totp("001", 0L, "");
        Totp t0_blank_2 = new Totp("001", 0L, "");
        Totp t0_null = new Totp("001", 0L, null);
        Totp t0_null_2 = new Totp("001", 0L, null);

        Totp tNull_0_0 = new Totp(null, 0L, "0");
        Totp tNull_0_0_ = new Totp(null, 0L, "0");

        assertFalse(t0_0.equals(null));
        assertFalse(t0_0.equals(new Object()));
        assertTrue(t0_0.equals(t0_0));
        assertTrue(t0_0.equals(t0_0_2));
        assertTrue(t0_0_2.equals(t0_0));

        assertTrue(tNull_0_0.equals(tNull_0_0_));
        assertFalse(tNull_0_0.equals(t0_0));
        assertFalse(t0_0.equals(tNull_0_0));

        t0_0.isAuthenticated(true);
        assertFalse(t0_0.equals(t0_0_2));

        assertFalse(t0_0.equals(t0_1));
        assertFalse(t0_0.equals(t1_1));
        assertFalse(t0_0.equals(t0_blank));
        assertFalse(t0_0.equals(t0_null));
        assertFalse(t0_blank.equals(t0_0));
        assertFalse(t0_null.equals(t0_0));

        assertFalse(t0_blank.equals(t0_null));
        assertTrue(t0_blank.equals(t0_blank_2));
        assertTrue(t0_null.equals(t0_null_2));
    }

    /**
     * 002: void hashCode() のテストメソッドです. equals()でtrueとなる場合、hashCode()が同一であることをテストする。
     * ワンタイムパスワードのnull、空文字の違いはhashCode()では区別されず、同一のハッシュ値となることをテストする。
     */
    @Test
    public void testhashCode() {
        Totp t0_0 = new Totp("001", 0L, "0");
        Totp t0_0_2 = new Totp("001", 0L, "0");
        Totp t0_1 = new Totp("001", 0L, "1");
        Totp t1_1 = new Totp("001", 1L, "1");
        Totp t0_blank = new Totp("001", 0L, "");
        Totp t0_blank_2 = new Totp("001", 0L, "");
        Totp t0_null = new Totp("001", 0L, null);
        Totp t0_null_2 = new Totp("001", 0L, null);

        assertTrue(t0_0.hashCode() == t0_0.hashCode());
        assertTrue(t0_0.hashCode() == t0_0_2.hashCode());
        assertTrue(t0_0_2.hashCode() == t0_0.hashCode());

        t0_0.isAuthenticated(true);
        assertFalse(t0_0.hashCode() == t0_0_2.hashCode());

        assertFalse(t0_0.hashCode() == t0_1.hashCode());
        assertFalse(t0_0.hashCode() == t1_1.hashCode());
        assertFalse(t0_0.hashCode() == t0_blank.hashCode());
        assertFalse(t0_0.hashCode() == t0_null.hashCode());

        assertTrue(t0_blank.hashCode() == t0_null.hashCode());
        assertTrue(t0_blank.hashCode() == t0_blank_2.hashCode());
        assertTrue(t0_null.hashCode() == t0_null_2.hashCode());
    }

    /**
     * 003: long getTimeCounter() のテストメソッドです. コンストラクタで指定したタイムカウンタが取得できることをテストします。
     */
    @Test
    public void testGetTimeCounter() {
        long expected = 1000L;
        Totp t = new Totp("001", expected, "123456789");
        long actual = t.getTimeCounter();
        assertEquals(expected, actual);
    }

    /**
     * 004: String getTotp() のテストメソッドです. コンストラクタで指定したTOTP文字列が取得できることをテストします。
     */
    @Test
    public void testGetTotp() {
        String expected = "123456789";
        Totp t = new Totp("001", 1000L, expected);
        String actual = t.getTotp();
        assertEquals(expected, actual);
    }

    /**
     * 005: boolean isAuthenticated() のテストメソッドです. 初期値がfalseで取得できることをテストします。
     */
    @Test
    public void testIsAuthenticated_get001() {
        boolean expected = false;
        Totp t = new Totp("001", 1L, "1");
        boolean actual = t.isAuthenticated();
        assertTrue(expected == actual);
    }

    /**
     * 006: boolean isAuthenticated() のテストメソッドです. setterで設定したtrueが取得できることをテストします。
     */
    @Test
    public void testIsAuthenticated_get002() {
        boolean expected = true;
        Totp t = new Totp("001", 1L, "1");
        t.isAuthenticated(expected);
        boolean actual = t.isAuthenticated();
        assertTrue(expected == actual);
    }

    /**
     * 007: boolean isAuthenticated() のテストメソッドです. setterで設定したfalseが取得できることをテストします。
     */
    @Test
    public void testIsAuthenticated_get003() {
        boolean expected = false;
        Totp t = new Totp("001", 1L, "1");
        t.isAuthenticated(expected);
        boolean actual = t.isAuthenticated();
        assertTrue(expected == actual);
    }

    /**
     * 008: String getImmutableUserId() のテストメソッドです. コンストラクタで指定したユーザ固有IDが取得できることをテストします。
     */
    @Test
    public void testGetImmutableUserId() {
        String expected = "001";
        Totp t = new Totp(expected, 100L, "123456789");
        String actual = t.getImmutableUserId();
        assertEquals(expected, actual);
    }
}
