/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.totp;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TOTPを生成します.
 * @author SIOS Technology, Inc.
 */
public class TotpGenerator {

    /** MACアルゴリズムを表す定数です. */
    private static final String MAC_ALGORITHM = "HMACSHA1";

    /** Class logger. */
    private final Logger _logger = LoggerFactory.getLogger(this.getClass());

    /** 秘密鍵バイト配列. */
    private final byte[] _secretKeyByteArray;

    /** タイムカウンタを表します. */
    private final long _timeCounter;

    /** ワンタイムパスワード桁数を表します. */
    private final int _codeLength;

    /**
     * 指定されたタイムカウンタ、ワンタイムパスワード桁数でインスタンスを生成します.
     * @param secretKeyByteArray 秘密鍵バイト配列
     * @param timeCounter タイムカウンタ
     * @param codeLength ワンタイムパスワード桁数
     */
    public TotpGenerator(byte[] secretKeyByteArray, long timeCounter, int codeLength) {
        this._secretKeyByteArray = secretKeyByteArray;
        this._timeCounter = timeCounter;
        this._codeLength = codeLength;
        _logger.debug("TOTP生成条件 (タイムカウント：{} TOTP桁数：{})", timeCounter, codeLength);
    }

    /**
     * ワンタイムパスワードを生成します.
     * @return ワンタイムパスワード文字列
     * @throws GeneralSecurityException TOTP生成に失敗した場合
     */
    public String generateTotp() throws GeneralSecurityException {

        // CHECKSTYLE:OFF マジックナンバー例外

        byte[] value = ByteBuffer.allocate(8).putLong(_timeCounter).array();

        final Mac mac = Mac.getInstance(MAC_ALGORITHM);
        mac.init(new SecretKeySpec(_secretKeyByteArray, ""));
        byte[] hash = mac.doFinal(value);
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);

        // CHECKSTYLE:ON マジックナンバー例外

        final double powBase = 10d;
        int otp = binary % (int) Math.pow(powBase, _codeLength);
        return padOutput(otp);
    }

    /**
     * 整数値をワンタイムパスワード桁数分の左ゼロ埋めした文字列を返します.
     * @param value 整数値
     * @return 左ゼロ埋めしたワンタイムパスワード文字列
     */
    private String padOutput(int value) {
        String result = Integer.toString(value);
        for (int i = result.length(); i < _codeLength; i++) {
            result = "0" + result;
        }
        return result;
    }
}
