/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.totp;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TOTP用のタイムカウンタを表すクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpCounter {

    /** 1秒のミリ秒表現です. */
    private static final int SECOND_IN_MILLIS = 1000;

    /** Class logger. */
    private final Logger _logger = LoggerFactory.getLogger(this.getClass());

    /** タイムステップ (秒) を表します. */
    private long _timeStepSec;

    /** UNIX時間の開始 ミリ秒を表します. */
    private long _startTimeMillis;

    /**
     * 指定されたタイムステップ (秒) とUNIX開始時間を0ミリ秒で初期化されたインスタンスを生成します.
     * @param timeStepSec タイムステップ (秒)
     */
    public TotpCounter(long timeStepSec) {
       this(timeStepSec, 0L);
    }

    /**
     * 指定されたタイムステップ (秒) とUNIX開始ミリ秒で初期化されたインスタンスを生成します.
     * @param timeStepSec タイムステップ (秒)
     * @param startTimeMillis UNIX時間の開始ミリ秒
     */
    public TotpCounter(long timeStepSec, long startTimeMillis) {
        if (timeStepSec < 1) {
            throw new IllegalArgumentException(
                    MessageFormat.format("タイムステップは1以上の整数値である必要があります。 (タイムステップ：{0})", timeStepSec));
        }
        _timeStepSec = timeStepSec;
        _startTimeMillis = startTimeMillis;
    }

    /**
     * タイムカウントを取得します.
     * @return タイムカウント値
     */
    public long getTimeCount() {

        long timeStepMilliSec = _timeStepSec * SECOND_IN_MILLIS;
        long currentUnixTimeMilliSec = System.currentTimeMillis();

        _logger.debug(MessageFormat.format(
                "UNIX time (ms): {0}, Start time (ms): {1}, Time step (ms): {2}",
                currentUnixTimeMilliSec, _startTimeMillis, timeStepMilliSec));

        return (currentUnixTimeMilliSec - _startTimeMillis) / timeStepMilliSec;
    }

}
