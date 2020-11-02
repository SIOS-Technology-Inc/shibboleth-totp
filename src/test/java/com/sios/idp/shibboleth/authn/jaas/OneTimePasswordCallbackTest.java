/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.authn.jaas;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

//import com.sios.idp.shibboleth.authn.jaas.OneTimePasswordCallback;

/**
 * {@link com.sios.idp.shibboleth.authn.jaas.OneTimePasswordCallback}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class OneTimePasswordCallbackTest {

    /**
     * 001: String getPrompt() のテストメソッドです. コンストラクタで指定した文字列が取得できることをテストします。
     */
    @Test
    public void testGetPrompt() {
        String expected = "prompt";
        OneTimePasswordCallback callback = new OneTimePasswordCallback(expected);
        assertEquals(expected, callback.getPrompt());
    }

    /**
     * 002: String getOneTimePassword() のテストメソッドです. setterで設定した値がgetterで取得できることをテストします。
     */
    @Test
    public void testGetOneTimePassword() {
        String expected = "123456789";
        OneTimePasswordCallback callback = new OneTimePasswordCallback("prompt");
        callback.setOneTimePassword(expected);
        assertEquals(expected, callback.getOneTimePassword());
    }
}
