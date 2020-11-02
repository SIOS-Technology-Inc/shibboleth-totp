/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.exception;

/**
 * TOTP認証セッションID生成時のエラーを示す例外です.
 * @author SIOS Technology, Inc.
 */
public class TotpAuthnSessionIdGenerationException extends UnrecoverableException {

    /** シリアルバージョンIDです. */
    private static final long serialVersionUID = 1L;

    /**
     * インスタンスを生成します..
     */
    public TotpAuthnSessionIdGenerationException() {
        super();
    }

    /**
     * 指定したメッセージで初期化されたインスタンスを生成します.
     * @param message メッセージ
     */
    public TotpAuthnSessionIdGenerationException(final String message) {
        super(message);
    }

    /**
     * 指定したメッセージとエラー原因で初期化されたインスタンスを生成します.
     * @param message メッセージ
     * @param cause エラー原因
     */
    public TotpAuthnSessionIdGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * 指定したエラー原因で初期化されたインスタンスを生成します.
     * @param cause エラー原因
     */
    public TotpAuthnSessionIdGenerationException(final Throwable cause) {
        super(cause);
    }
}
