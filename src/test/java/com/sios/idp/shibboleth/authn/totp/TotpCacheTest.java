/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF テストクラスのため
package com.sios.idp.shibboleth.authn.totp;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import mockit.Mocked;
import mockit.Expectations;

import org.junit.Test;

import com.sios.idp.shibboleth.common.AppConfig;

/**
 * {@link com.sios.idp.shibboleth.authn.totp.TotpCache}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpCacheTest {

    @Mocked
    final AppConfig _appConfig = null;

    /**
     * テストケース間の依存関係を無くすためSingletonを初期化します.
     */
    private void prepareCache() {
        try {
            // private static final なSingletonを変更可能なフィールドにする
            Field instanceField = TotpCache.class.getDeclaredField("TOTP_CACHE");
            instanceField.setAccessible(true);
            int modifiers = instanceField.getModifiers();
            Field modifierField = instanceField.getClass().getDeclaredField("modifiers");
            modifiers = modifiers & ~Modifier.FINAL;
            modifierField.setAccessible(true);
            modifierField.setInt(instanceField, modifiers);

            // private なデフォルトコンストラクタからインスタンスを生成
            Constructor<TotpCache> privateConstructor = TotpCache.class.getDeclaredConstructor();
            privateConstructor.setAccessible(true);
            TotpCache obj = privateConstructor.newInstance();

            // private static final なSingletonに新規インスタンスをセット
            instanceField.set(null, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 001: TotpCache getInstance() のテストメソッドです. 取得されるインスタンスが同一であることをテストします。
     */
    @Test
    public void testGetInstance() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        TotpCache target1 = TotpCache.getInstance();
        TotpCache target2 = TotpCache.getInstance();
        TotpCache target3 = TotpCache.getInstance();
        assertTrue(target1 == target2);
        assertTrue(target1 == target3);
        assertTrue(target2 == target3);
    }

    /**
     * 002: boolean isAvailable() のテストメソッドです.
     * addしたTotpオブジェクトの場合true、addしていないTotpオブジェクトの場合falseが返却されることをテストします。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void test0IsAvailable001() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        Totp t1 = new Totp("user1", 1L, "111111");
        Totp t2 = new Totp("user1", 2L, "222222");
        Totp t3 = new Totp("user1", 3L, "333333");
        TotpCache target = TotpCache.getInstance();

        // キャッシュ0件の場合falseになることを確認
        assertFalse(target.isAvailable("user1", t1));

        target.add("user1", t1);
        target.add("user1", t2);
        assertTrue(target.isAvailable("user1", t1));
        assertTrue(target.isAvailable("user1", t2));

        // インスタンスが別でもtrueになることを確認
        Totp t1Another = new Totp("user1", 1L, "111111");
        Totp t2Another = new Totp("user1", 2L, "222222");

        assertTrue(target.isAvailable("user1", t1Another));
        assertTrue(target.isAvailable("user1", t2Another));
        assertFalse(target.isAvailable("user2", t1));
        assertFalse(target.isAvailable("user2", t2));
        assertFalse(target.isAvailable("user1", t3));
    }

    /**
     * 003: boolean isAvailable() のテストメソッドです.
     * addしたTotpオブジェクトの場合true、addしていないTotpオブジェクトの場合falseが返却されることをテストします。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void testIsAvailable002() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        Totp t1 = new Totp("user1", 1L, "111111");
        Totp t2 = new Totp("user1", 2L, "222222");
        Totp t3 = new Totp("user1", 3L, "333333");
        Totp t4 = new Totp("user1", 4L, "444444");
        Totp t5 = new Totp("user1", 5L, "555555");
        TotpCache target = TotpCache.getInstance();
        target.add("user1", t1);
        target.add("user1", t2);
        target.add("user1", t3);
        target.add("user1", t4);
        target.add("user1", t5);

        TotpStorage totpStorage = null;
        try {
            Field f = target.getClass().getDeclaredField("totpStorage");
            f.setAccessible(true);
            totpStorage = (TotpStorage) f.get(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5, getCount(totpStorage, "user1"));
        assertTrue(target.isAvailable("user1", t1));
        assertEquals(5, getCount(totpStorage, "user1"));
        assertTrue(target.isAvailable("user1", t2));
        assertEquals(5, getCount(totpStorage, "user1"));
        assertTrue(target.isAvailable("user1", t3));
        assertEquals(4, getCount(totpStorage, "user1"));
        assertTrue(target.isAvailable("user1", t4));
        assertEquals(3, getCount(totpStorage, "user1"));
        assertTrue(target.isAvailable("user1", t5));
        assertEquals(2, getCount(totpStorage, "user1"));

        Totp t6 = new Totp("user1", 6L, "666666");
        target.add("user1", t6);
        assertTrue(target.isAvailable("user1", t5));
        assertEquals(3, getCount(totpStorage, "user1"));
    }

    /**
     * 003: boolean isAvailable() のテストメソッドです.
     * 1つ前のタイムカウンタのTOTPは有効、2つ以上前のタイムカウンタのTOTPは無効と判定されることをテストします。
     * また、2つ以上前のタイムカウンタのTOTPはTotpCacheから削除されることをテストします。
     * レコード件数を取得するAPIは提供していないため、リフレクションで内部のtotpStorageの件数を取得して確認します。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void testIsAvailable003() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        Totp t1 = new Totp("user1", 1L, "111111");
        Totp t2 = new Totp("user1", 2L, "222222");
        Totp t3 = new Totp("user1", 3L, "333333");
        Totp t4 = new Totp("user1", 4L, "444444");
        Totp t5 = new Totp("user1", 5L, "555555");
        TotpCache target = TotpCache.getInstance();
        target.add("user1", t1);
        target.add("user1", t2);
        target.add("user1", t3);
        target.add("user1", t4);
        target.add("user1", t5);

        TotpStorage totpStorage = null;
        try {
            Field f = target.getClass().getDeclaredField("totpStorage");
            f.setAccessible(true);
            totpStorage = (TotpStorage) f.get(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5, getCount(totpStorage, "user1"));
        Totp t5_3 = new Totp("user1", 5L, "333333");
        assertFalse(target.isAvailable("user1", t5_3));
        assertEquals(2, getCount(totpStorage, "user1"));

        Totp t5_4 = new Totp("user1", 5L, "444444");
        assertTrue(target.isAvailable("user1", t5_4));
        assertEquals(2, getCount(totpStorage, "user1"));
    }

    /**
     * 004: boolean isAvailable() のテストメソッドです.
     * 2つ前のタイムカウンタのTOTPは有効、3つ以上前のタイムカウンタのTOTPは無効と判定されることをテストします。
     * また、3つ以上前のタイムカウンタのTOTPはTotpCacheから削除されることをテストします。
     * レコード件数を取得するAPIは提供していないため、リフレクションで内部のtotpStorageの件数を取得して確認します。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void testIsAvailable004() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 2;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        Totp t1 = new Totp("user1", 1L, "111111");
        Totp t2 = new Totp("user1", 2L, "222222");
        Totp t3 = new Totp("user1", 3L, "333333");
        Totp t4 = new Totp("user1", 4L, "444444");
        Totp t5 = new Totp("user1", 5L, "555555");
        TotpCache target = TotpCache.getInstance();
        target.add("user1", t1);
        target.add("user1", t2);
        target.add("user1", t3);
        target.add("user1", t4);
        target.add("user1", t5);

        TotpStorage totpStorage = null;
        try {
            Field f = target.getClass().getDeclaredField("totpStorage");
            f.setAccessible(true);
            totpStorage = (TotpStorage) f.get(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5, getCount(totpStorage, "user1"));
        Totp t5_2 = new Totp("user1", 5L, "222222");
        assertFalse(target.isAvailable("user1", t5_2));
        assertEquals(3, getCount(totpStorage, "user1"));

        Totp t5_3 = new Totp("user1", 5L, "333333");
        assertTrue(target.isAvailable("user1", t5_3));
        assertEquals(3, getCount(totpStorage, "user1"));

        Totp t5_4 = new Totp("user1", 5L, "444444");
        assertTrue(target.isAvailable("user1", t5_4));
        assertEquals(3, getCount(totpStorage, "user1"));
    }

    /**
     * boolean isAvailable() のテストメソッドです. timeStepSec x (allowedTimeCountOffset + 1) +
     * totpCacheExpirationBufferSecが負の数に なる場合、全てのタイムカウントのTOTPはキャッシュされず、無効と判定されることをテストします。
     * レコード件数を取得するAPIは提供していないため、リフレクションで内部のtotpStorageの件数を取得して確認します。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void testIsAvailableTotpCacheExpirationBufferSecMinus() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = -1;
                AppConfig.getAllowedTimeCountOffset();
                result = 2;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = -30;
            }
        };

        prepareCache();

        Totp t1 = new Totp("user1", 1L, "111111");
        Totp t2 = new Totp("user1", 2L, "222222");
        Totp t3 = new Totp("user1", 3L, "333333");
        Totp t4 = new Totp("user1", 4L, "444444");
        Totp t5 = new Totp("user1", 5L, "555555");
        TotpCache target = TotpCache.getInstance();
        target.add("user1", t1);
        target.add("user1", t2);
        target.add("user1", t3);
        target.add("user1", t4);
        target.add("user1", t5);

        TotpStorage totpStorage = null;
        try {
            Field f = target.getClass().getDeclaredField("totpStorage");
            f.setAccessible(true);
            totpStorage = (TotpStorage) f.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        try {
            assertEquals(0, getCount(totpStorage, "user1"));
            Totp t5_2 = new Totp("user1", 5L, "222222");
            assertFalse(target.isAvailable("user1", t5_2));
            assertEquals(0, getCount(totpStorage, "user1"));

            Totp t5_3 = new Totp("user1", 5L, "333333");
            assertFalse(target.isAvailable("user1", t5_3));
            assertEquals(0, getCount(totpStorage, "user1"));

            Totp t5_4 = new Totp("user1", 5L, "444444");
            assertFalse(target.isAvailable("user1", t5_4));
            assertEquals(0, getCount(totpStorage, "user1"));
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * 004: boolean isAvailable() のテストメソッドです. allowedTimeCountOffsetが負数の場合、ゼロとして扱われ
     * 現在のタイムカウンタのTOTPのみが有効と判定されることをテストします。 また、1つ以上前のタイムカウンタのTOTPはTotpCacheから削除されることをテストします。
     * レコード件数を取得するAPIは提供していないため、リフレクションで内部のtotpStorageの件数を取得して確認します。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void testIsAvailable005() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = -1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        Totp t1 = new Totp("user1", 1L, "111111");
        Totp t2 = new Totp("user1", 2L, "222222");
        Totp t3 = new Totp("user1", 3L, "333333");
        Totp t4 = new Totp("user1", 4L, "444444");
        Totp t5 = new Totp("user1", 5L, "555555");
        TotpCache target = TotpCache.getInstance();
        target.add("user1", t1);
        target.add("user1", t2);
        target.add("user1", t3);
        target.add("user1", t4);
        target.add("user1", t5);

        TotpStorage totpStorage = null;
        try {
            Field f = target.getClass().getDeclaredField("totpStorage");
            f.setAccessible(true);
            totpStorage = (TotpStorage) f.get(target);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Totp t5_4 = new Totp("user1", 5L, "444444");
        assertFalse(target.isAvailable("user1", t5_4));
        assertEquals(1, getCount(totpStorage, "user1"));

        Totp t5_5 = new Totp("user1", 5L, "555555");
        assertTrue(target.isAvailable("user1", t5_5));
        assertEquals(1, getCount(totpStorage, "user1"));
    }

    /**
     * 007: Totp get() のテストメソッドです. add()したTotpオブジェクトが取得できることをテストします。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void testGet001() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        long timeCounter1 = 1L;
        long timeCounter2 = 2L;
        long timeCounter3 = 3L;
        String totp = "123456";
        String userName = "user1";
        Totp t1 = new Totp(userName, timeCounter1, totp);
        Totp t2 = new Totp(userName, timeCounter2, "222222");
        Totp t3 = new Totp(userName, timeCounter3, "333333");
        TotpCache target = TotpCache.getInstance();
        target.add(userName, t1);
        target.add(userName, t2);
        target.add("user2", t3);

        Totp actual1 = target.get(userName, timeCounter1);
        Totp actual2 = target.get(userName, timeCounter2);
        Totp actual3 = target.get(userName, timeCounter3); // userName違いのためnullが期待される返却値
        assertEquals(t1, actual1);
        assertEquals(t2, actual2);
        assertNull(actual3);
    }

    /**
     * 008: Totp get() のテストメソッドです.
     * タイムカウントが負数のTotpオブジェクトをadd()した場合、IllegalArgumentExceptionがthrowされることをテストします。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGet002() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        long timeCounterPlus = 1L;
        long timeCounterZero = 0L;
        long timeCounterMinus = -1L;
        String totp = "123456";
        String userName = "user1";
        Totp t1 = new Totp(userName, timeCounterPlus, totp);
        Totp t2 = new Totp(userName, timeCounterZero, totp);
        Totp t3 = new Totp(userName, timeCounterMinus, totp);
        TotpCache target = TotpCache.getInstance();
        target.add(userName, t1);
        target.add(userName, t2);
        target.add(userName, t3);
        fail("例外が発生するはず");
    }

    /**
     * 009: boolean exists() のテストメソッドです.
     * addしたTotpオブジェクトの場合true、addしていないTotpオブジェクトの場合falseが返却されることをテストします。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void testExists001() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        Totp t1 = new Totp("user1", 1L, "111111");
        Totp t2 = new Totp("user1", 2L, "222222");
        Totp t3 = new Totp("user3", 3L, "333333");
        TotpCache target = TotpCache.getInstance();

        target.add("user1", t1);
        target.add("user1", t2);
        target.add("user2", t3);

        System.out.println("target.toString()=" + target.toString());

        assertTrue(target.exists("user1", t1.getTimeCounter()));
        assertTrue(target.exists("user1", t2.getTimeCounter()));
        assertFalse(target.exists("userX", t2.getTimeCounter())); // ユーザ名違い
        assertFalse(target.exists("user2", t1.getTimeCounter())); // タイムカウント違い
        assertFalse(target.exists("user2", 999L)); // ユーザ名・タイムカウント違い

        // インスタンスが別でもtrueになることを確認
        Totp t1Another = new Totp("user1", 1L, "111111");
        assertTrue(target.exists("user1", t1Another.getTimeCounter()));
    }

    /**
     * 010: boolean exists() のテストメソッドです.
     * addしたTotpオブジェクトの場合true、addしていないTotpオブジェクトの場合falseが返却されることをテストします。
     * TotpCacheオブジェクトはSingletonのため、他のテストメソッドに影響を受ける点に注意してください。
     */
    @Test
    public void testExists002() {

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30;
                AppConfig.getAllowedTimeCountOffset();
                result = 1;
                AppConfig.getTotpCacheExpirationBufferSec();
                result = 30;
            }
        };

        prepareCache();

        Totp t1 = new Totp("user1", 1L, "111111");
        Totp t2 = new Totp("user1", 2L, "222222");
        Totp t3 = new Totp("user2", 3L, "333333");
        TotpCache target = TotpCache.getInstance();
        target.add("user1", t1);
        target.add("user1", t2);
        target.add("user2", t3);
        assertTrue(target.exists("user1", t1.getTimeCounter(), 0));
        assertFalse(target.exists("user1", t1.getTimeCounter(), 1));

        assertTrue(target.exists("user1", t2.getTimeCounter(), 1));
        assertFalse(target.exists("user1", t2.getTimeCounter(), 2));

        assertFalse(target.exists("userX", t2.getTimeCounter(), 1)); // ユーザ名違い
        assertFalse(target.exists("user2", t1.getTimeCounter(), 1)); // タイムカウント違い
        assertFalse(target.exists("user2", 999L, 1)); // ユーザ名・タイムカウント違い

        // インスタンスが別でもtrueになることを確認
        Totp t1Another = new Totp("user1", 1L, "111111");
        assertTrue(target.exists("user1", t1Another.getTimeCounter()));
    }

    private int getCount(TotpStorage totpStorage, String userName) {
        Iterator keys = totpStorage.getKeys(userName);
        int cnt = 0;
        if (keys == null) { return 0; }
        while (keys.hasNext()) {
            keys.next();
            cnt++;
        }
        return cnt;
    }
}
