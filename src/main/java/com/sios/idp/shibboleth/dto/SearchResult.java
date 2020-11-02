/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * サーチ結果の格納とサーチ結果を取得するためのクラスです.
 * @author SIOS Technology, Inc.
 */
public class SearchResult {
    /** LDAPのサーチ結果を格納するためのMap型変数を定義します. */
    private Map<String, String> addMap = new HashMap<String, String>();

    /**
     * インスタンスを生成します.
     */
    public SearchResult() {
    }

    /**
     * サーチ結果を格納するためのメソッドです.
     * @param key 検索を行なう際に利用する属性名
     * @param value 検索を行なう際に実際に返されるValue名
     */
    public void add(String key, String value) {
        this.addMap.put(key, value);
    }

    /**
     * 指定されたキーに対応した値を取得します.
     * @param key 検索時の属性名
     * @return 指定されたキーに対応した値を返します.
     */
    public String getValue(String key) {
        return getAttrString(this.addMap, key);
    }

    /**
     * プライベートメソッド 渡されたLDAPからの実行結果と渡されたキーに対応した値を検索し、呼び出し元へ検索結果を返します.
     * @param resultMap サーチ結果が格納されているMap
     * @param key Valueの値を引っ張るための属性名
     * @return 格納された結果から渡されたキーで検索をかけた結果を文字列として返す
     */
    private String getAttrString(Map<String, String> resultMap, String key) {
        String result = null;
        if (resultMap != null) {
            result = resultMap.get(key);
        }
        return result;
    }
}
