/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.common.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

/**
 * {@link com.sios.idp.shibboleth.common.util.ExceptionUtil}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class ExceptionUtilTest {

    /**
     * 001: String stackTraceToString() のテストメソッドです. スタックトレース文字列が取得されることを標準出力で確認します。
     */
    @Test
    public void testStackTraceToString001() {

        String actual = null;
        try {
            throw new Exception("例外が発生しました");
        } catch (Exception e) {
            actual = ExceptionUtil.stackTraceToString(e);
        }
        System.out.println(actual);
    }

    /**
     * 002: String stackTraceToString() のテストメソッドです. 入れ子例外のスタックトレース文字列が取得されることを標準出力で確認します。
     */
    @Test
    public void testStackTraceToString002() {

        String actual = null;
        try {
            throw new NumberFormatException("NumberFormatExceptionが発生しました");
        } catch (NumberFormatException e) {
            try {
                throw new Exception("Exceptionが発生しました", e);
            } catch (Exception e2) {
                actual = ExceptionUtil.stackTraceToString(e2);
            }
        }
        System.out.println(actual);
    }

    /**
     * 003: String stackTraceToString() のテストメソッドです. StringWriterのclose()でIOExceptionが発生した場合、
     * RuntimeExceptionがthrowされることをテストします。
     */
    @Test
    public void testStackTraceToString003() {

        new MockUp<StringWriter>() {
            @Mock
            void close() throws IOException {
                throw new IOException();
            }
        };

        String actual = null;
        try {
            throw new NumberFormatException("NumberFormatExceptionが発生しました");
        } catch (NumberFormatException e) {
            try {
                actual = ExceptionUtil.stackTraceToString(e);
                fail("例外が発生するはず");
            } catch (RuntimeException e2) {
                assertEquals(e2.getClass(), RuntimeException.class);
            }
        }
        System.out.println(actual);
    }

    /**
     * 004: String stackTraceToString() のテストメソッドです. StringWriterのインスタンス生成で例外が発生した場合、
     * 戻り値がnullであることをテストします。
     */
    @Test
    public void testStackTraceToString004() {

        new MockUp<StringWriter>() {
            @Mock
            void $init() {
                throw new RuntimeException();
            }
        };

        String actual = null;
        try {
            throw new NumberFormatException("NumberFormatExceptionが発生しました");
        } catch (NumberFormatException e) {
            try {
                actual = ExceptionUtil.stackTraceToString(e);
                fail("例外が発生するはず");
            } catch (RuntimeException e2) {
                assertEquals(e2.getClass(), RuntimeException.class);
            }
        }
        assertNull(actual);
    }

    /**
     * 004: String stackTraceToString() のテストメソッドです. PrintWriterのインスタンス生成で例外が発生した場合、
     * 戻り値がnullであることをテストします。
     */
    @Test
    public void testStackTraceToString005() {

        new MockUp<PrintWriter>() {
            @Mock
            void $init(Writer out) {
                throw new RuntimeException();
            }
        };

        String actual = null;
        try {
            throw new NumberFormatException("NumberFormatExceptionが発生しました");
        } catch (NumberFormatException e) {
            try {
                actual = ExceptionUtil.stackTraceToString(e);
                fail("例外が発生するはず");
            } catch (RuntimeException e2) {
                assertEquals(e2.getClass(), RuntimeException.class);
            }
        }
        assertNull(actual);
    }
}
