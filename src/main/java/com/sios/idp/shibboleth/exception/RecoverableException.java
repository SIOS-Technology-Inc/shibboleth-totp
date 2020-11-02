/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.exception;

/**
 * ユーザにてリカバリ可能なエラーの場合に返す例外クラスです.
 * @author SIOS Technology, Inc.
 */
public class RecoverableException extends Exception {
    /** シリアライズバージョンIDです. */
    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成します.
     */
    public RecoverableException() {
        super();
    }

    /**
     * 指定したメッセージで初期化されたインスタンスを生成します.
     * @param message メッセージ
     */
    public RecoverableException(final String message) {
        super(message);
    }

    /**
     * 指定したメッセージとエラー原因で初期化されたインスタンスを生成します.
     * @param message メッセージ
     * @param throwable エラー原因
     */
    public RecoverableException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 指定したエラー原因で初期化されたインスタンスを生成します.
     * @param throwable エラー原因
     */
    public RecoverableException(Throwable throwable) {
        super(throwable);
    }
}
