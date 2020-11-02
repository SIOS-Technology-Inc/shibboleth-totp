/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.exception.ConfigInitializationException;

/**
 * サーブレットコンテキストのコンテキストパラメータをロードします.<br>
 * ロードした情報を格納する{@link com.sios.idp.shibboleth.common.Config}の全てのフィールド名が
 * &lt;context-param&gt;タグ内の&lt;param-name&gt;タグに同一名で定義されている必要があります。
 * Config実装クラスのフィールドはString型のみがロード対象フィールドとして扱われます。
 * @author SIOS Technology, Inc.
 */
public class AppConfigLoader implements ConfigLoadable {

    /**
     * String型からキーのクラス型に変換するためのメソッド名を定義したマップです.
     * Stringを引数に該当クラス型に変換可能なメソッドを指定する必要があります。
     */
    @SuppressWarnings("serial")
    private static final Map<Class<?>, String> CONVERT_METHOD_NAMES
        = new HashMap<Class<?>, String>() {
            {
                put(String.class, null);
                put(Integer.class, "valueOf");
                put(Long.class, "valueOf");
                put(Double.class, "valueOf");
            }
    };
    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** サーブレットコンテキストを表します. */
    private ServletContext _ctx;

    /**
     * 設定情報を取得するサーブレットコンテキストを指定してインスタンスを生成します.
     * @param ctx 設定情報取得対象のサーブレットコンテキスト
     */
    public AppConfigLoader(ServletContext ctx) {
        this._ctx = ctx;
    }

    /** {@inheritDoc} */
    @Override
    public void load(Class<? extends Config> configClass) throws ConfigInitializationException {
        // Integer, Long
        try {
            Field[] fields = configClass.getDeclaredFields();
            for (Field f : fields) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                Class<?> fieldType = f.getType();
                if (CONVERT_METHOD_NAMES.containsKey(fieldType)) {
                    String convertMethodName = CONVERT_METHOD_NAMES.get(fieldType);
                    Object val = getConfigValue(f.getName());
                    if (convertMethodName != null) {
                        Method m = fieldType.getMethod(convertMethodName, String.class);
                        val = m.invoke(null, val);
                    }
                    f.set(null, val);
                    logger.debug(MessageFormat.format(
                          "AppConfig type: {0} key: {1} value: {2}",
                          fieldType.getName(), f.getName(), val));
                }
            }
        } catch (Exception e) {
            throw new ConfigInitializationException(e);
        }
    }

    /**
     * 指定されたキーに対応する値をサーブレットコンテキストから取得します.
     * @param key キー
     * @return 設定値
     * @throws ConfigInitializationException {@link com.sios.shibboleth.idp.common.Config}のフィールド名と
     *      同名のパラメータがサーブレットコンテキストに定義されていない場合
     */
    private String getConfigValue(String key) throws ConfigInitializationException {
        String value = _ctx.getInitParameter(key);
        if (value == null) {
            throw new ConfigInitializationException(MessageFormat.format(
                    "Configurationクラスで期待されるServletCotextパラメータ {0} が定義されていません。", key));
        }
        value = value.trim();
        if (value.trim() == "") {
            logger.warn(MessageFormat.format("ServletCotextパラメータ：{0} の値がブランクです。", key));
        }
        return value;
    }

}
