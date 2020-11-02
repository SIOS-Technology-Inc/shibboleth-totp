/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256ハッシュ化のためのクラスです.
 * @author SIOS Technology, Inc.
 */
public final class Sha256 {
    /** ハッシュ化する際のアルゴリズムを定義します. */
    public static final String ALGORITHM = "SHA-256";
    /** getBytesする際の文字コードを定義します. */
    public static final String ENCODE = "UTF-8";

    /**
     * Sha256プライベートコンストラクタです.
     */
    private Sha256() {
    }

    /**
     * 指定文字列をハッシュ化します.
     * @param value ハッシュ化するString
     * @return ハッシュ化されたbyte配列
     * @throws NoSuchAlgorithmException アルゴリズムが利用不可能です.
     * @throws UnsupportedEncodingException  文字エンコードが利用不可能です.
     */
    public static byte[] hash(String value)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        return md.digest(value.getBytes(ENCODE));
    }
}
