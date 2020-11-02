/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.exception;

/**
 * ユーザがリカバリ不可能なエラーが出現した場合に投げられる例外クラスです.
 * @author SIOS Technology, Inc.
 */
public class UnrecoverableException extends Exception {
    /** シリアルバージョンIDです. */
    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成します.
     */
    public UnrecoverableException() {
        super();
    }

    /**
     * 指定したメッセージで初期化されたインスタンスを生成します.
     * @param message メッセージ
     */
    public UnrecoverableException(final String message) {
        super(message);
    }

    /**
     * 指定したメッセージとエラー原因で初期化されたインスタンスを生成します.
     * @param message メッセージ
     * @param throwable エラー原因
     */
    public UnrecoverableException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 指定したエラー原因で初期化されたインスタンスを生成します.
     * @param throwable エラー原因
     */
    public UnrecoverableException(final Throwable throwable) {
        super(throwable);
    }
}
