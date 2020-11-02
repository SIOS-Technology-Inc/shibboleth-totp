/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common;

/**
 * アプリケーション設定を保持するクラスです.
 * @author SIOS Technology, Inc.
 */
public final class AppConfig implements Config {

    /** LDAPの接続先URLを定義します. */
    private static String ldapUrl;

    /** LDAPアクセス時のバインドDNを定義します. */
    private static String ldapBindDN;

    /** LDAPアクセス時のユーザパスワードを定義します. */
    private static String ldapBindPassword;

    /** LDAPへSEARCHを実行する際のBASE DNを定義します. */
    private static String ldapBaseDN;

    /** LDAPへサーチを実行する際のクエリを定義します. */
    private static String ldapFilter;

    /** 暗号アルゴリズムを定義します. */
    private static String cipherArgorithm;

    /** SALTの値を生成するベースの長さを定数として定義します. */
    private static Integer saltLength;

    /** PBEKeySpecを作成するために行なう繰り返し回数を定義します. */
    private static Integer iterationCount;

    /** 暗号化・複合化する際のbit数を定義します. */
    private static Integer cipherKeyLength;

    /** 暗号化するために必要なインスタンスを定義します. */
    private static String cipherTransformationName;

    /** ユーザ固有IDのLDAP属性名を表します. */
    private static String immutableIdAttributeName;

    /** 秘密鍵のLDAP属性名を表します. */
    private static String secretKeyAttributeName;

    /** 秘密鍵パスフレーズを表します. */
    private static String secretKeyPassPhrase;

    /** {@link com.sios.idp.shibboleth.datasource.dao.Dao}の実装クラスの完全修飾名を表します. */
    private static String daoImplClassName;

    /** TOTP生成時のタイムステップサイズ (秒) を表します. */
    private static Long timeStepSec;

    /** TOTP桁数を表します. */
    private static Integer totpLength;

    /** TOTP認証時に許容するタイムカウントのオフセット値です. */
    private static Integer allowedTimeCountOffset;

    /** TOTPがキャッシュに存在する時間(秒)です. */
    private static Long totpCacheExpirationBufferSec;

    /** TOTP認証セッションIDを記憶するCookieの名前です. */
    private static String totpAuthnSessionIdCookieName;

    /** TOTP認証セッションの発行日時を記憶するCookieの名前です. */
    private static String totpAuthnSessionIssueDateCookieName;

    /** TOTP認証セッションの有効期限（秒）です. 0や負の値の場合、TOTP認証セッションを利用しません. */
    private static Integer totpAuthnSessionExpirationSec;

    /** TOTP認証セッションIDを発行する際に利用するソルト文字列です. */
    private static String totpAuthnSessionIdSalt;

    /**
     * privateコンストラクタ. 外部からのインスタンス生成を許可しません.
     */
    private AppConfig() { }

    /**
     * 暗号アルゴリズムを取得します.
     * @return cipherArgorithm 暗号アルゴリズム
     */
    public static String getCipherArgorithm() {
        return cipherArgorithm;
    }

    /**
     * LDAP接続先URLを取得します.
     * @return LDAP接続先URL
     */
    public static String getLdapUrl() {
        return ldapUrl;
    }

    /**
     * LDAPアクセス時のバインドDNを取得します.
     * @return LDAPのバインドDN
     */
    public static String getLdapBindDN() {
        return ldapBindDN;
    }

    /**
     * LDAPアクセス時のパスワードを取得します.
     * @return LDAPアクセス時のパスワード
     */
    public static String getLdapBindPassword() {
        return ldapBindPassword;
    }

    /**
     * LDAP検索時のベースDNを取得します.
     * @return LDAP検索時のベースDN
     */
    public static String getLdapBaseDN() {
        return ldapBaseDN;
    }

    /**
     * LDAP検索フィルタを取得します.
     * @return LDAP検索フィルタ
     */
    public static String getLdapFilter() {
        return ldapFilter;
    }

    /**
     * SALTを生成する長さを取得します.
     * @return SALTの長さ
     */
    public static Integer getSaltLength() {
        return saltLength;
    }

    /**
     * PBRKeySpecを生成するための繰り返し回数を取得します.
     * @return 繰り返し回数
     */
    public static Integer getIterationCount() {
        return iterationCount;
    }

    /**
     * 暗号化・複合化する際のcipher keyの長さを取得します.
     * @return key length
     */
    public static Integer getCipherKeyLength() {
        return cipherKeyLength;
    }

    /**
     * 暗号化するために必要なインスタンスを取得します.
     * @return インスタンス
     */
    public static String getCipherTransformationName() {
        return cipherTransformationName;
    }

    /**
     * ユーザ固有IDを取得します.
     * @return immutableIdAttributeName ユーザ固有IDのLDAP属性名
     */
    public static String getImmutableUserIdAttributeName() {
        return immutableIdAttributeName;
    }

    /**
     * 秘密鍵のLDAP属性名を取得します.
     * @return 秘密鍵のLDAP属性名
     */
    public static String getSecretKeyAttributeName() {
        return secretKeyAttributeName;
    }

    /**
     * 秘密鍵パスフレーズを取得します.
     * @return 秘密鍵パスフレーズ
     */
    public static String getSecretKeyPassPhrase() {
        return secretKeyPassPhrase;
    }

    /**
     * {@link com.sios.idp.shibboleth.datasource.dao.Dao}実装クラスの完全修飾名を取得します.
     * @return daoImplClassName DAO実装クラスの完全修飾名
     */
    public static String getDaoImplClassName() {
        return daoImplClassName;
    }

    /**
     * TOTP生成に使用するタイムステップサイズ (秒) を取得します.
     * @return タイムステップサイズ (秒)
     */
    public static Long getTimeStepSec() {
        return timeStepSec;
    }

    /**
     * TOTP桁数を取得します.
     * @return TOTP桁数
     */
    public static Integer getTotpLength() {
        return totpLength;
    }

    /**
     * TOTP認証時に許容されるタイムカウントのオフセット値を取得します.
     * @return 許容されるタイムカウントのオフセット値
     */
    public static Integer getAllowedTimeCountOffset() {
        return allowedTimeCountOffset;
    }

    /**
     * TOTPをキャッシュする時間 （秒）を取得します.
     * @return タイムステップサイズ (秒)
     */
    public static Long getTotpCacheExpirationBufferSec() {
        return totpCacheExpirationBufferSec;
    }

    /**
     * TOTP認証セッションIDを記憶するCookieの名前を取得します.
     * @return セッションIDを記憶するCookieの名前
     */
    public static String getTotpAuthnSessionIdCookieName() {
        return totpAuthnSessionIdCookieName;
    }

    /**
     * TOTP認証セッションの発行日時を記憶するCookieの名前を取得します.
     * @return セッションの発行日時を記憶するCookieの名前
     */
    public static String getTotpAuthnSessionIssueDateCookieName() {
        return totpAuthnSessionIssueDateCookieName;
    }

    /**
     * TOTP認証セッションの有効期限（秒）を取得します.
     * @return セッションの有効期限（秒）
     */
    public static Integer getTotpAuthnSessionExpirationSec() {
        return totpAuthnSessionExpirationSec;
    }

    /**
     * TOTP認証セッションIDを発行する際に利用するソルト文字列を取得します.
     * @return ソルト文字列
     */
    public static String getTotpAuthnSessionIdSalt() {
        return totpAuthnSessionIdSalt;
    }

}
