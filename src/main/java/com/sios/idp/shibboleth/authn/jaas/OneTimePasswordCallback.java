/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.jaas;

import java.io.Serializable;

import javax.security.auth.callback.Callback;

/**
 * ワンタイムパスワード用Callback.
 * @author SIOS Technology, Inc.
 */
public class OneTimePasswordCallback implements Callback , Serializable {

    /** Serial Version UID を表します. */
    private static final long serialVersionUID = -2204692368963278756L;

    /** プロンプトを表します. */
    private String _prompt;

    /** ワンタイムパスワードを表します. */
    private String _oneTimePassword;

    /**
     * 指定したプロンプトでインスタンスを生成します.
     * @param prompt プロンプト
     * */
    public OneTimePasswordCallback(String prompt) {
        this._prompt = prompt;
    }

    /**
     * ワンタイムパスワードをクリアします.
     */
    public void clearOneTimePassword() {
        _oneTimePassword = null;
    }

    /**
     * プロンプトを取得します.
     * @return プロンプト
     */
    public String getPrompt() {
        return _prompt;
    }

    /**
     * ワンタイムパスワードを取得します.
     * @return ワンタイムパスワード
     */
    public String getOneTimePassword() {
        return _oneTimePassword;
    }

    /**
     *ワンタイムパスワードを設定します.
     * @param oneTimePassword ワンタイムパスワード
     */
    public void setOneTimePassword(String oneTimePassword) {
        this._oneTimePassword = oneTimePassword;
    }
}
