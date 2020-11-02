/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.totp;

import com.sios.idp.shibboleth.exception.TotpGenerationException;

/**
 * TOTPプロバイダのインタフェースです.
 * {@link com.sios.idp.shibboleth.authn.jaas.TotpLoginModule}に
 * {@link com.sios.idp.shibboleth.authn.totp.Totp}オブジェクトを提供するクラスは
 * 本インタフェースを実装する必要があります。
 * @author SIOS Technology, Inc.
 */
public interface TotpProvider {

    /**
     * TOTPオブジェクトを取得します.
     * @param userName ユーザ名
     * @return TOTPオブジェクト
     * @throws TotpGenerationException TOTP生成に失敗した場合
     */
    Totp getTotp(String userName)
            throws TotpGenerationException;

}
