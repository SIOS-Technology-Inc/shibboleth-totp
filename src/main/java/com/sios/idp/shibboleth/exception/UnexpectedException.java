/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.exception;

/**
 * 予期せぬエラーが原因で投げられるException用のクラスです.
 * @author SIOS Technology, Inc.
 */
public class UnexpectedException extends UnrecoverableException {
    /** シリアルバージョンIDです. */
    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成します.
     */
    public UnexpectedException() {
        super();
    }

    /**
     * 指定したメッセージで初期化されたインスタンスを生成します.
     * @param message メッセージ
     */
    public UnexpectedException(final String message) {
        super(message);
    }

    /**
     * 指定したメッセージとエラー原因で初期化されたインスタンスを生成します.
     * @param message メッセージ
     * @param throwable 原因
     */
    public UnexpectedException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 指定したエラー原因で初期化されたインスタンスを生成します.
     * @param throwable 原因
     */
    public UnexpectedException(final Throwable throwable) {
        super(throwable);
    }
}
