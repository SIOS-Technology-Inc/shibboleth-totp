/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.authn.totp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

//import com.sios.idp.shibboleth.authn.totp.TotpCounter;

/**
 * {@link com.sios.idp.shibboleth.authn.totp.TotpCounter}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpCounterTest {

    /**
     * 001: long getTimeCount() のテストメソッドです. 開始時間を指定せずにインスタンスを生成した場合、0ミリ秒としてタイムカントが算出されることをテストします。
     */
    @Test
    public void testGetTimeCount001() {
        long timeStep = 30L;
        TotpCounter target = new TotpCounter(timeStep);
        long count = target.getTimeCount();
        assertEquals(count, (System.currentTimeMillis() - 0L) / (timeStep * 1000));
    }

    /**
     * 002: long getTimeCount() のテストメソッドです. 指定したタイムステップと開始時間でタイムカントが算出されることをテストします。
     */
    @Test
    public void testGetTimeCount002() {
        long timeStep = 30L;
        TotpCounter target = new TotpCounter(timeStep, 10L);
        long count = target.getTimeCount();
        assertEquals(count, (System.currentTimeMillis() - 10L) / (timeStep * 1000));
    }

    /**
     * 003: long getTimeCount() のテストメソッドです. 指定したタイムステップと開始時間でタイムカントが算出されることをテストします。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTimeCount003() {
        long timeStep = -1L;
        TotpCounter target = new TotpCounter(timeStep, 10L);
        long count = target.getTimeCount();
        assertEquals(count, (System.currentTimeMillis() - 10L) / (timeStep * 1000));
    }
}
