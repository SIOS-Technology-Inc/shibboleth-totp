/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common;

import com.sios.idp.shibboleth.exception.ConfigInitializationException;

/**
 * {@link com.sios.idp.shibboleth.common.Config}に設定情報をロード可能なクラスが
 * 実装するインタフェースです.
 * @author SIOS Technology, Inc.
 */
public interface ConfigLoadable {

    /**
     * 指定された{@link com.sios.idp.shibboleth.common.Config}に設定情報をロードします.
     * @param configClass 設定情報保持オブジェクト
     * @throws ConfigInitializationException 設定オブジェクトで定義されているフィールド名に対応する
     *  外部設定情報が存在しない場合。その他、設定情報をロード時の予期せぬエラーが発生した場合
     */
    void load(Class<? extends Config> configClass) throws ConfigInitializationException;
}
