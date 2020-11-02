/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.authn.totp;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.AppConfig;

/**
 * ユーザ固有ID、タイムカウンタ毎のTOTPキャッシュ機能を提供します.<br>
 * TOTPの保持の仕方：
 * &lt;String immutableUserId, &lt;Long timeCounter,
 * com.sios.shibboleth.idp.auth.totp.Totp totp&gt;&gt;<br>
 * キャッシュ機能そのものは{@link com.sios.idp.shibboleth.authn.totp.TotpStorage}に処理を委譲します。
 * @author SIOS Technology, Inc.
 */
public final class TotpCache {

    /** Class logger. */
    private final Logger _logger = LoggerFactory.getLogger(this.getClass());

    /** Singletonなインスタンス. */
    private static final TotpCache TOTP_CACHE = new TotpCache();

    /** キャッシュ機能の委譲先クラス. */
    private TotpStorage totpStorage = new TotpStorage();

    /**
     * privateコンストラクタです.
     * 外部からのインスタンス生成を許可しません。
     */
    private TotpCache() {
    }

    /**
     * TOTPキャッシュオブジェクトを取得します.
     * @return TOTPキャッシュオブジェクト
     */
    public static TotpCache getInstance() {
        return TOTP_CACHE;
    }

    /**
     * 指定されたユーザ固有IDでTOTPオブジェクトを追加します.
     * @param immutableUserId ユーザ固有ID
     * @param totp TOTPオブジェクト
     */
    public void add(String immutableUserId, Totp totp) {

        long timeCounter = totp.getTimeCounter();
        if (timeCounter < 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "TOTP生成に使用するタイムカウンタは正の整数値である必要があります。{0}", timeCounter));
        }
        totpStorage.put(immutableUserId, timeCounter, totp);
    }

    /**
     * 指定されたユーザ固有IDとTOTPオブジェクトが有効なTOTPであるかを取得します.
     * 本クラスで保持している全てのキャッシュを比較対象とするのではなく
     * タイムカウントが{@link com.sios.idp.shibboleth.common.AppConfig}の
     * allowedTimeCountOffsetの範囲内のTotpオブジェクトを比較対象とします。
     * @param immutableUserId ユーザ固有ID
     * @param totp TOTPオブジェクト
     * @return 指定されたユーザ固有IDのTOTPオブジェクトを含む場合はtrue、含まない場合はfalse
     */
    public boolean isAvailable(String immutableUserId, Totp totp) {
        _logger.debug("TOTP有効判定 ユーザ固有ID：{}, 現在のタイムカウンタ：{}, TOTP：{}",
                immutableUserId, totp.getTimeCounter(), totp.getTotp());
        synchronized (totpStorage) {
            List<String> totps = getAvailableTotps(immutableUserId, totp.getTimeCounter());
            return totps.contains(totp.getTotp());
        }

    }

    /**
     * 指定されたユーザ固有ID、タイムカウントのTOTPがキャッシュに存在するかどうかを取得します.
     * 指定タイムカウントから{@link com.sios.idp.shibboleth.common.AppConfig}の
     * allowedTimeCountOffset値を差し引いたタイムカウントまでのそれぞれのタイムカウント毎のTOTPが
     * 1つでもキャッシュされていない場合はfalseが取得されます。
     * @param immutableUserId ユーザ固有ID
     * @param timeCounter タイムカウント
     * @return 指定されたユーザ固有IDについて指定タイムカウントから{@link com.sios.idp.shibboleth.common.AppConfig}の
     *      allowedTimeCountOffset値を差し引いたタイムカウントまでのそれぞれのタイムカウント毎のTOTPが
     *      1つでもキャッシュに存在しない場合はfalse、全てキャッシュに存在している場合はtrue
     */
    boolean exists(String immutableUserId, long timeCounter) {
        _logger.debug("TOTP存在チェック ユーザ固有ID：{}, 現在のタイムカウンタ：{}", immutableUserId, timeCounter);
        return totpStorage.contains(immutableUserId, timeCounter);
    }

    /**
     * 指定されたユーザ固有ID、タイムカウントのTOTPがキャッシュに存在するかどうかを取得します.
     * 指定タイムカウントから{@link com.sios.idp.shibboleth.common.AppConfig}の
     * allowedTimeCountOffset値を差し引いたタイムカウントまでのそれぞれのタイムカウント毎のTOTPが
     * 1つでもキャッシュされていない場合はfalseが取得されます。
     * @param immutableUserId ユーザ固有ID
     * @param timeCounter タイムカウント
     * @param timeCounterOffset 有効なTOTPとして許容するタイムカウントオフセット値
     * @return 指定されたユーザ固有IDについて指定タイムカウントから{@link com.sios.idp.shibboleth.common.AppConfig}の
     *      allowedTimeCountOffset値を差し引いたタイムカウントまでのそれぞれのタイムカウント毎のTOTPが
     *      1つでもキャッシュに存在しない場合はfalse、全てキャッシュに存在している場合はtrue
     */
    boolean exists(String immutableUserId, long timeCounter, int timeCounterOffset) {

        _logger.debug("TOTP存在チェック ユーザ固有ID：{}, 現在のタイムカウンタ：{}, タイムカウントオフセット値：{}",
                immutableUserId, timeCounter, timeCounterOffset);
        for (int i = 0; i <= timeCounterOffset; i++) {
            if (!exists(immutableUserId, timeCounter - i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定されたユーザ固有ID、タイムカウントのTOTPオブジェクトを取得しします.
     * @param immutableUserId ユーザ固有ID
     * @param timeCounter タイムカウント
     * @return TOTPオブジェクト
     */
    Totp get(String immutableUserId, long timeCounter) {
        return totpStorage.get(immutableUserId, timeCounter);
    }

    /**
     * 利用可能なTOTP文字列のコレクションを取得します.
     * 次の条件を全て満たすものを有効なTOTPとして取得します。
     * <ul>
     * <li>指定されたタイムカウンタのTOTP、及び任意の数(※)以前までのタイムカウンタのTOTP
     * (※) {@link com.sios.idp.shibboleth.common.AppConfig｝のallowedTimeCountOffset値に従います。</li>
     * <li>一度も認証に使用されていないTOTPであること</li>
     * </ul>
     * @param immutableUserId ユーザ固有ID
     * @param timeCounter タイムカウンタ
     * @return 利用可能なTOTPオブジェクトのコレクション
     */
    private List<String> getAvailableTotps(String immutableUserId, long timeCounter) {

        int cntOffset = AppConfig.getAllowedTimeCountOffset();
        _logger.debug(
                "TOTP認証を許容するタイムカウントのオフセット設定値 allowedTimeCountOffset：{}", cntOffset);
        if (cntOffset < 0) {
            cntOffset = 0;
            _logger.warn(
                "TOTP認証を許容するタイムカウントのオフセット設定値が負数のため、ゼロとして扱います。 ");
        }
        long allowedTimeCount = timeCounter - cntOffset;
        Iterator<Long> timeCounters = totpStorage.getKeys(immutableUserId);

        if (timeCounters == null) {
            return new ArrayList<String>();
        }
        List<String> totps = new ArrayList<String>();
        while (timeCounters.hasNext()) {
            long time = timeCounters.next();
            if (allowedTimeCount <= time) {
                Totp t = totpStorage.get(immutableUserId, time);
                // 認証済みのものは除外
                if (t != null && !t.isAuthenticated()) {
                    totps.add(t.getTotp());
                    _logger.debug("有効なTOTP ユーザ固有ID：{} タイムカウント：{} TOTP：{}",
                            immutableUserId, t.getTimeCounter(), t.getTotp());
                }
            } else {
                // 許容するタイムカウントよりも前のTOTPは無効なため削除
                totpStorage.remove(immutableUserId, time);
                _logger.debug("破棄されたTOTP ユーザ固有ID：{} タイムカウント：{}", immutableUserId, time);
            }
        }
        return totps;
    }

}
