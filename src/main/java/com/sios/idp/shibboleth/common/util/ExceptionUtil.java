/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 例外クラス関連のユーティリティメソッドを提供します.
 * @author SIOS Technology, Inc.
 */
public final class ExceptionUtil {

    /**
     * インスタンス生成を許可しません.
     */
    private ExceptionUtil() {
    }

    /**
     * スタックトレースを文字列化します.
     * @param throwable スタックトレース出力対象
     * @return スタックトレース文字列
     */
    public static String stackTraceToString(Throwable throwable) {

        StringWriter sw = null;
        PrintWriter pw = null;
        String stackTrace = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.flush();
            stackTrace = sw.toString();
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (pw != null) {
                pw.close();
            }
        }
        return stackTrace;
    }
}
