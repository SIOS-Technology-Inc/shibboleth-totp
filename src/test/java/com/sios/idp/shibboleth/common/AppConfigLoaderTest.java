/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.common;

import static org.junit.Assert.*;

import javax.servlet.ServletContext;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;

import com.sios.idp.shibboleth.exception.ConfigInitializationException;

/**
 * {@link com.sios.idp.shibboleth.common.AppConfigLoader}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class AppConfigLoaderTest {

    @Mocked
    ServletContext ctx;

    private static final class MockAppConfigSingle implements Config {

        private static String str;

        private MockAppConfigSingle() {
        }

        public static String getStr() {
            return str;
        }
    }

    /**
     * 001: void load(Config config)のテストメソッドです.
     * モックServletContextのパラメータ名からConfigの同名フィールドにデータがロードできることをテストする。（単一・Stringフィールド）
     * @throws ConfigInitializationException コンテキストパラメータ時に予期せぬエラーが発生した場合 コンテキストパラメータ時に予期せぬエラーが発生した場合
     */
    @Test
    public void testLoad001() throws ConfigInitializationException {

        final String expected = "strValue";

        new Expectations() {
            {
                ctx.getInitParameter("str");
                result = expected;
            }
        };

        AppConfigLoader loader = new AppConfigLoader(ctx);
        loader.load(MockAppConfigSingle.class);

        assertEquals(expected, MockAppConfigSingle.getStr());
    }

    private static final class MockAppConfigMultiple implements Config {

        private static Integer int1;
        private static Double dbl2;
        private static String str3;
        private static Long lng4;

        public static Integer getInt1() {
            return int1;
        }

        public static Double getDbl2() {
            return dbl2;
        }

        public static String getStr3() {
            return str3;
        }

        public static Long getLng4() {
            return lng4;
        }

        private MockAppConfigMultiple() {
        }

    }

    /**
     * 002: void load(Config config)のテストメソッドです.
     * モックServletContextのパラメータ名からConfigの同名フィールドにデータがロードできることをテストする。（複数・型混在フィールド）
     * @throws ConfigInitializationException コンテキストパラメータ時に予期せぬエラーが発生した場合
     */
    @Test
    public void testLoad002() throws ConfigInitializationException {

        final Integer expected1 = 10;
        final Double expected2 = 0.01d;
        final String expected3 = "strValue3";
        final Long expected4 = 30L;

        new Expectations() {
            {
                ctx.getInitParameter("int1");
                result = expected1;
                ctx.getInitParameter("dbl2");
                result = expected2;
                ctx.getInitParameter("str3");
                result = expected3;
                ctx.getInitParameter("lng4");
                result = expected4;
            }
        };

        AppConfigLoader loader = new AppConfigLoader(ctx);
        loader.load(MockAppConfigMultiple.class);

        assertEquals(expected1, MockAppConfigMultiple.getInt1());
        assertEquals(expected2, MockAppConfigMultiple.getDbl2());
        assertEquals(expected3, MockAppConfigMultiple.getStr3());
        assertEquals(expected4, MockAppConfigMultiple.getLng4());
    }

    /**
     * 003: void load(Config config)のテストメソッドです. モックServletContextのパラメータ名に対応するConfigの同名フィールドが存在しない場合、
     * 存在するフィールドのみロードされることをテストする。（複数・型混在フィールド）
     * @throws ConfigInitializationException コンテキストパラメータ時に予期せぬエラーが発生した場合
     */
    @Test
    public void testLoad003() throws ConfigInitializationException {

        final Integer expected1 = 10;
        final Double expected2 = 0.01d;
        final String expected3 = "strValue3";
        final Long expected4 = 30L;
        //final String expected5 = "strValue4";

        new Expectations() {
            {
                ctx.getInitParameter("int1");
                result = expected1;
                ctx.getInitParameter("dbl2");
                result = expected2;
                ctx.getInitParameter("str3");
                result = expected3;
                ctx.getInitParameter("lng4");
                result = expected4;
                //ctx.getInitParameter("str4");
                //result = expected5;
            }
        };

        AppConfigLoader loader = new AppConfigLoader(ctx);
        loader.load(MockAppConfigMultiple.class);

        assertEquals(expected1, MockAppConfigMultiple.getInt1());
        assertEquals(expected2, MockAppConfigMultiple.getDbl2());
        assertEquals(expected3, MockAppConfigMultiple.getStr3());
    }

    /**
     * 004: void load(Config config)のテストメソッドです. モックServletContextのパラメータ名にブランク値が含まれる場合、
     * 警告ログ「"ServletCotextパラメータ： {0} の値がブランクです。"」が出力されることを確認する。
     * @throws ConfigInitializationException コンテキストパラメータ時に予期せぬエラーが発生した場合
     */
    @Test
    public void testLoad004() throws ConfigInitializationException {

        final Integer expected1 = 10;
        final Double expected2 = 0.01d;
        final String expected3 = "";
        final Long expected4 = 30L;

        new Expectations() {
            {
                ctx.getInitParameter("int1");
                result = expected1;
                ctx.getInitParameter("dbl2");
                result = expected2;
                ctx.getInitParameter("str3");
                result = expected3;
                ctx.getInitParameter("lng4");
                result = expected4;
            }
        };

        AppConfigLoader loader = new AppConfigLoader(ctx);
        loader.load(MockAppConfigMultiple.class);

        assertEquals(expected1, MockAppConfigMultiple.getInt1());
        assertEquals(expected2, MockAppConfigMultiple.getDbl2());
        assertEquals(expected3, MockAppConfigMultiple.getStr3());
        assertEquals(expected4, MockAppConfigMultiple.getLng4());
    }

    /**
     * 005: void load(Config config)のテストメソッドです.
     * AppConfigフィールド名に対応するモックServletContextの同名パラメータが存在しない場合
     * ConfigInitializationExceptionがthrowされることを確認する。
     * @throws ConfigInitializationException コンテキストパラメータ時に予期せぬエラーが発生した場合
     */
    @Test(expected = ConfigInitializationException.class)
    public void testLoad005() throws ConfigInitializationException {

        final Integer expected1 = 10;
        final Double expected2 = 0.01d;
        final String expected3 = "";

        new Expectations() {
            {
                ctx.getInitParameter("int1");
                result = expected1;
                ctx.getInitParameter("dbl2");
                result = expected2;
            }
        };

        AppConfigLoader loader = new AppConfigLoader(ctx);
        loader.load(MockAppConfigMultiple.class);

        assertEquals(expected1, MockAppConfigMultiple.getInt1());
        assertEquals(expected2, MockAppConfigMultiple.getDbl2());
        assertEquals(expected3, MockAppConfigMultiple.getStr3());
    }

    /**
     * 006: void load(Config config)のテストメソッドです.
     * AppConfigフィールドの型と互換性のない値がモックServletContextのパラメータに設定されている場合
     * ConfigInitializationExceptionがthrowされることを確認する。
     * @throws ConfigInitializationException コンテキストパラメータ時に予期せぬエラーが発生した場合
     */
    @Test(expected = ConfigInitializationException.class)
    public void testLoad006() throws ConfigInitializationException {

        final String expected1 = "AAA";
        final Double expected2 = 0.01d;
        final String expected3 = "";

        new Expectations() {
            {
                ctx.getInitParameter("int1");
                result = expected1;
                //ctx.getInitParameter("dbl2");
                //result = expected2;
            }
        };

        AppConfigLoader loader = new AppConfigLoader(ctx);
        loader.load(MockAppConfigMultiple.class);

        assertEquals(expected1, MockAppConfigMultiple.getInt1());
        assertEquals(expected2, MockAppConfigMultiple.getDbl2());
        assertEquals(expected3, MockAppConfigMultiple.getStr3());
    }

}
