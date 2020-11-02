/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.totp;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Time-based One-Time Password (TOTP)を表現するクラスです.
 * @author SIOS Technology, Inc.
 */
public class Totp implements Serializable {

    /** Serial Version UIDです. */
    private static final long serialVersionUID = -5280785772753905662L;

    /** ユーザ固有IDです. */
    private final String _immutableUserId;

    /** タイムカウンタです. */
    private final long _timeCounter;

    /** ワンタイムパスワードです. */
    private final String _totp;

    /** 認証済みフラグです. */
    private boolean _isAuthenticated = false;

    /**
     * 指定されたユーザ固有ID、タイムカウンタ、ワンタイムパスワードでインスタンスを生成します.
     * @param immutableUserId ユーザ固有ID
     * @param timeCounter タイムカウンタ
     * @param totp ワンタイムパスワード
     */
    public Totp(String immutableUserId, long timeCounter, String totp) {
        this._immutableUserId = immutableUserId;
        this._timeCounter = timeCounter;
        this._totp = totp;
    }

    @Override
    /** @inheritDoc */
    public boolean equals(Object o) {

        if (this == o) { return true; }
        if (o == null || !(o instanceof Totp)) {
            return false;
        }
        Totp other = (Totp) o;
        if (getImmutableUserId() == null) {
            if (other.getImmutableUserId() != null) { return false; }
        } else {
            if (!getImmutableUserId().equals(other.getImmutableUserId())) { return false; }
        }
        if (getTotp() == null) {
            if (other.getTotp() != null) { return false; }
        } else {
            if (!getTotp().equals(other.getTotp())) { return false; }
        }
        if (getTimeCounter() != other.getTimeCounter()) { return false; }
        if (isAuthenticated() != other.isAuthenticated()) { return false; }
        return true;
    }

    @Override
    /** @inheritDoc */
    public int hashCode() {
        return Arrays.hashCode(
                new Object[] {
                        getImmutableUserId(), getTimeCounter(), getTotp(), isAuthenticated()
                        });
    }

    /**
     * ユーザ固有IDを取得します.
     * @return ユーザ固有ID
     */
    public String getImmutableUserId() {
        return _immutableUserId;
    }

    /**
     * タイムカウンタを取得します.
     * @return タイムカウンタ
     */
    public long getTimeCounter() {
        return _timeCounter;
    }

    /**
     * ワンタイムパスワード文字列を取得します.
     * @return ワイタイムパスワード文字列
     */
    public String getTotp() {
        return _totp;
    }

    /**
     * 認証済みフラグを取得します.
     * @return 認証済みの場合true、認証済みでない場合false
     */
    public boolean isAuthenticated() {
        return _isAuthenticated;
    }

    /**
     * 認証済みフラグを設定します.
     * @param isAuthenticated 認証済みの場合true、認証済みでない場合false
     */
    public void isAuthenticated(boolean isAuthenticated) {
        _isAuthenticated = isAuthenticated;
    }

}
