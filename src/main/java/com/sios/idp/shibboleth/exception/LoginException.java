/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.exception;

/**
 * Login関連でエラーが出た際に投げられるExceptionクラスです.
 * @author SIOS Technology, Inc.
 */
public class LoginException extends RecoverableException {
    /** シリアルバージョンIDです. */
    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成します.
     */
    public LoginException() {
        super();
    }

    /**
     * 指定したメッセージで初期化したインスタンスを生成します.
     * @param message メッセージ
     */
    public LoginException(final String message) {
        super(message);
    }

    /**
     * 指定したメッセージとエラー原因で初期化したインスタンスを生成します.
     * @param message メッセージ
     * @param throwable エラー原因
     */
    public LoginException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 指定したエラー原因で初期化したインスタンスを生成します.
     * @param throwable エラー原因
     */
    public LoginException(final Throwable throwable) {
        super(throwable);
    }
}
