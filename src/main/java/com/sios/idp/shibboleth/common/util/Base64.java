/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

/**
 * Base64暗号化・複合化クラスです.
 * @author SIOS Technology, Inc.
 */
public final class Base64 {
    /**
     * Base64プライベートコンストラクタです.
     */
    private Base64() {
    }

    /**
     * 指定byte列をBASE64エンコードします.
     * @param value 暗号化するbyte列
     * @return String
     */
    public static String encode(byte[] value) {
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(value));
    }

    /**
     * Base64でStringをデコードするための処理です. サポート対応外の文字列などを渡すと
     * IllegalArgumentExceptionを投げる.
     * @param value 複合化するためvalue
     * @return byte[]
     */
    public static byte[] decode(String value) {
        byte[] result = null;
        if (!org.apache.commons.codec.binary.Base64.isBase64(value)) {
            throw new IllegalArgumentException("不正な文字列が挿入されている可能性があります。");
        }
        result = org.apache.commons.codec.binary.Base64.decodeBase64(value);
        return result;
    }
}
