/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.exception;

/**
 * 接続エラーなど構築環境が関連して投げられるExceptionクラスです.
 * @author SIOS Technology, Inc.
 */
public class UserDuplicatedException extends UnrecoverableException {
    /** シリアルバージョンIDです. */
    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成します.
     */
    public UserDuplicatedException() {
        super();
    }

    /**
     * 指定したメッセージで初期化されたインスタンスを生成します.
     * @param message メッセージ
     */
    public UserDuplicatedException(final String message) {
        super(message);
    }

    /**
     * 指定したメッセージとエラー原因で初期化されたインスタンスを生成します.
     * @param message メッセージ
     * @param throwable エラー原因
     */
    public UserDuplicatedException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 指定したエラー原因で初期化されたインスタンスを生成します.
     * @param throwable 原因
     */
    public UserDuplicatedException(final Throwable throwable) {
        super(throwable);
    }
}
