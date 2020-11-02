/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.totp;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sios.idp.shibboleth.common.AppConfig;

/**
 * メモリキャッシュのラッパークラスです. メモリキャッシュに対する操作を行います
 * @author SIOS Technology, Inc.
 */
public class TotpStorage {
    /** キャッシュ. */
    private Cache<String, Map<Long, Totp>> cache;

    /**
     * コンストラクタ.
     */
    public TotpStorage() {
        cache = CacheBuilder.newBuilder().expireAfterWrite(getExpireAfterWrite(), TimeUnit.SECONDS).build();
    }

    /**
     * 指定されたユーザID、タイムカウンタをキーにキャッシュにワンタイムパスワードが存在するかどうかを確認します.
     * @param immutableUserId ユーザID
     * @param timeCounter タイムカウンタ
     * @return True:キャッシュTotpが存在する/False:キャッシュにTotpが存在しない
     */
    public boolean contains(String immutableUserId, long timeCounter) {
        Map<Long, Totp> map = cache.getIfPresent(immutableUserId);
        if (map != null) {
            if (map.containsKey(timeCounter)) { return true; }
        }
        return false;
    }

    /**
     * 指定されたユーザID、タイムカウンタをキーにワンタイムパスワードをキャッシュに格納します.
     * @param immutableUserId ユーザID
     * @param timeCounter タイムカウンタ
     * @param totp ワンタイムパスワード
     * @return ワンタイムパスワード
     */
    public Totp put(String immutableUserId, Long timeCounter, Totp totp) {
        synchronized (cache) {
            Map<Long, Totp> map = cache.getIfPresent(immutableUserId);
            if (map != null) {
                if (!map.containsKey(timeCounter)) {
                    map.put(timeCounter, totp);
                }
            } else {
                map = new ConcurrentHashMap<Long, Totp>();
                map.put(timeCounter, totp);
                cache.put(immutableUserId, map);
            }
        }
        return totp;
    }

    /**
     * 指定されたユーザID、タイムカウンタをキーにキャッシュからワンタイムパスワードを取得します.
     * @param immutableUserId ユーザID
     * @param timeCounter タイムカウンタ
     * @return ワンタイムパスワード
     */
    public Totp get(String immutableUserId, Long timeCounter) {
        Map<Long, Totp> map = cache.getIfPresent(immutableUserId);
        if (map != null) { return map.get(timeCounter); }
        return null;
    }

    /**
     * 指定されたユーザIDをキーに、もう一つのキーであるタイムカウンタを取得します.
     * @param immutableUserId ユーザID
     * @return タイムカウンタのイテレータ
     */
    public Iterator<Long> getKeys(final String immutableUserId) {
        Map<Long, Totp> map = cache.getIfPresent(immutableUserId);
        if (map != null) { return map.keySet().iterator(); }
        return null;
    }

    /**
     * 指定されたユーザIDとタイムカウンタをキーに該当するワンタイムパスワードをキャッシュから削除します.
     * @param immutableUserId ユーザID
     * @param timeCounter タイムカウンタ
     * @return ワンタイムパスワード
     */
    public Totp remove(String immutableUserId, Long timeCounter) {
        Map<Long, Totp> map = cache.getIfPresent(immutableUserId);
        if (map != null) { return map.remove(timeCounter); }
        return null;
    }

    /**
     * デバック用文字列化メソッド.
     * @return キャッシュを文字列化したもの
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iter = cache.asMap().keySet().iterator(); iter.hasNext();) {
            String userId = iter.next();
            Map<Long, Totp> map = cache.getIfPresent(userId);
            if (map != null) {
                for (Totp t : map.values()) {
                    sb.append("UId=").append(userId).append(" Tc = ").append(t.getTimeCounter()).append(" Totp=")
                            .append(t.getTotp()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * キャッシュの寿命を計算します.
     * @return キャッシュの寿命
     */
    private Long getExpireAfterWrite() {
        Integer allowedTimeCountOffset = AppConfig.getAllowedTimeCountOffset();
        if (allowedTimeCountOffset < 0) {
            allowedTimeCountOffset = 0;
        }
        if (AppConfig.getTimeStepSec() * (allowedTimeCountOffset + 1)
                + AppConfig.getTotpCacheExpirationBufferSec() < 0) { return 0L; }
        return AppConfig.getTimeStepSec() * (allowedTimeCountOffset + 1) + AppConfig.getTotpCacheExpirationBufferSec();
    }
}
