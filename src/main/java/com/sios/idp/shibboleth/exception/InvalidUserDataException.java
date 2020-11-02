/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.exception;

/**
 * ユーザ情報が不正であることを示す例外です.
 * @author SIOS Technology, Inc.
 */
public class InvalidUserDataException extends TotpGenerationException {

    /** シリアルバージョンIDです. */
    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成します..
     */
    public InvalidUserDataException() {
        super();
    }

    /**
     * 指定したメッセージで初期化されたインスタンスを生成します.
     * @param message メッセージ
     */
    public InvalidUserDataException(final String message) {
        super(message);
    }

    /**
     * 指定したメッセージとエラー原因で初期化されたインスタンスを生成します.
     * @param message メッセージ
     * @param cause エラー原因
     */
    public InvalidUserDataException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * 指定したエラー原因で初期化されたインスタンスを生成します.
     * @param cause エラー原因
     */
    public InvalidUserDataException(final Throwable cause) {
        super(cause);
    }
}
