/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

/**
 * Base32暗号化・複合クラスです.
 * @author SIOS Technology, Inc.
 */
public final class Base32 {
    /**
     * privateコンストラクタです. 外部からのインスタンス生成を許可しません。
     */
    private Base32() {
    }

    /**
     * 指定byte列をBASE32でエンコードします。 apache.commonsのBase32のlibraryを利用します.
     * @param value 暗号化するbyte文字列
     * @return String
     */
    public static String encode(byte[] value) {
        return new String(new org.apache.commons.codec.binary.Base32().encode(value));
    }

    /**
     * 指定文字列をBASE32でデコードします。 apache.commonsのBase32のlibraryを利用します.
     * @param value 複合化するString文字列
     * @return byte[]
     */
    public static byte[] decode(String value) {
        org.apache.commons.codec.binary.Base32 base32 = new org.apache.commons.codec.binary.Base32();
        if (!base32.isInAlphabet(value)) {
            throw new IllegalArgumentException("不正な文字列が挿入されている可能性があります。");
        }
        return base32.decode(value);
    }
}
