/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.authn.totp;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;

import mockit.Mocked;
import mockit.Expectations;

import org.junit.Before;
import org.junit.Test;

import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.common.util.SecretKeyDecrypter;
import com.sios.idp.shibboleth.datasource.dao.Dao;
import com.sios.idp.shibboleth.datasource.dao.DaoFactory;
import com.sios.idp.shibboleth.dto.SearchResult;
import com.sios.idp.shibboleth.exception.DaoInstantiationException;
import com.sios.idp.shibboleth.exception.DataAccessException;
import com.sios.idp.shibboleth.exception.SecretKeyDecryptionException;
import com.sios.idp.shibboleth.exception.TotpGenerationException;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;

/**
 * {@link com.sios.idp.shibboleth.authn.totp.TotpProviderImpl}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpProviderImplTest {

    private static final String IMMUTABLE_USER_ID_ATTR_NAME = "immutableUserIdAttr";
    private static final String SECRET_KEY_ATTR_NAME = "secretKeyldapAttr";

    @Mocked
    final AppConfig _appConfig = null;
    @Mocked
    final SecretKeyDecrypter _decrypter = null;
    @Mocked
    DaoFactory _daoFactory;
    @Mocked
    Dao _dao;
    @Mocked
    TotpGenerator _totpGenerator;

    /**
     * テストケース間の依存関係を無くすためSingletonを初期化します.
     * @throws ReflectiveOperationException リフレクションエラーの場合
     */
    @Before
    public void setUp() throws ReflectiveOperationException {

        // private static final なSingletonを変更可能なフィールドにする
        Field instanceField = TotpCache.class.getDeclaredField("TOTP_CACHE");
        instanceField.setAccessible(true);
        int modifiers = instanceField.getModifiers();
        Field modifierField = instanceField.getClass().getDeclaredField("modifiers");
        modifiers = modifiers & ~Modifier.FINAL;
        modifierField.setAccessible(true);
        modifierField.setInt(instanceField, modifiers);

        // private なデフォルトコンストラクタからインスタンスを生成
        Constructor<TotpCache> privateConstructor = TotpCache.class.getDeclaredConstructor();
        privateConstructor.setAccessible(true);
        TotpCache obj = privateConstructor.newInstance();

        // private static final なSingletonに新規インスタンスをセット
        instanceField.set(null, obj);
    }

    /**
     * 001: long getTotp() のテストメソッドです. Totpオブジェクトが正常に取得できることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp001() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final long timeStep = 30L;
        final int totpLength = 7;
        final String secretKeyStr = "AAAAAAAAA";
        final String secretKeyPass = "PASSWORD";
        final byte[] hash = { 117, -92 };
        final String otp = "123456";
        final long now = System.currentTimeMillis();
        final long timeCount = now / (timeStep * 1000);

        new Expectations() {
            {
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                AppConfig.getSecretKeyAttributeName();
                result = SECRET_KEY_ATTR_NAME;

                // 秘密鍵の復号化
                AppConfig.getSecretKeyPassPhrase();
                result = secretKeyPass;
                SecretKeyDecrypter.decrypt(secretKeyStr, secretKeyPass);
                result = hash;
                // _totpCounter.getTimeCount(); result = timeCount;

                AppConfig.getTimeStepSec();
                result = timeStep;
                AppConfig.getTotpLength();
                result = totpLength;
                _totpGenerator = new TotpGenerator(hash, timeCount, totpLength);
                _totpGenerator.generateTotp();
                result = otp;
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        Totp actual = provider.getTotp(userName);
        assertEquals(timeCount, actual.getTimeCounter());
        assertEquals(otp, actual.getTotp());
    }

    /**
     * 002: long getTotp() のテストメソッドです.
     * 秘密鍵を取得するDAOの生成に失敗した場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp002() throws Exception {

        final String userName = "user001";
        final long timeStep = 30L;

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = new DaoInstantiationException("DAO生成失敗");
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("DAOインタフェース {0} の実装クラスのインスタンス生成に失敗しました", Dao.class.getName()),
                    e.getMessage());
            assertTrue(e.getCause() instanceof DaoInstantiationException);
        }
    }

    /**
     * 003: long getTotp() のテストメソッドです.
     * 秘密鍵取得時にUserDuplicatedExceptionが発生した場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp003() throws Exception {

        final String userName = "user001";
        final long timeStep = 30L;
        final String secretKeyStr = "AAAAAAAAA";

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = new UserDuplicatedException("ユーザ情報が複数件存在します。");
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("ユーザ名：{0}のユーザ情報が複数件存在します。", userName), e.getMessage());
            assertTrue(e.getCause() instanceof UserDuplicatedException);
        }
    }

    /**
     * 004: long getTotp() のテストメソッドです.
     * 秘密鍵取得時にDataAccessExceptionが発生した場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp004() throws Exception {

        final String userName = "user001";
        final long timeStep = 30L;
        final String secretKeyStr = "AAAAAAAAA";

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = new DataAccessException("データアクセスに失敗しました。");
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("ユーザ名：{0}のユーザ情報取得時にデータアクセスエラーが発生しました。", userName), e.getMessage());
            assertTrue(e.getCause() instanceof DataAccessException);
        }
    }

    /**
     * 005: long getTotp() のテストメソッドです. 取得ユーザ情報がnullの場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp005() throws Exception {

        final String userName = "user001";
        final long timeStep = 30L;

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                _dao.getUser(userName);
                result = null;
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("ユーザ名：{0}のユーザ情報が取得できませんでした。", userName), e.getMessage());
            assertNull(e.getCause());
        }
    }

    /**
     * 006: long getTotp() のテストメソッドです. 取得した秘密鍵がnullの場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp006() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final long timeStep = 30L;

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(SECRET_KEY_ATTR_NAME, null);
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("秘密鍵の取得に失敗しました。", userName), e.getMessage());
            assertNull(e.getCause());
        }
    }

    /**
     * 007: long getTotp() のテストメソッドです. 取得した秘密鍵が空文字の場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp007() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final long timeStep = 30L;

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                searchResult.add(SECRET_KEY_ATTR_NAME, "");
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                AppConfig.getSecretKeyAttributeName();
                result = SECRET_KEY_ATTR_NAME;
                _dao.getUser(userName);
                result = searchResult;
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("秘密鍵の取得に失敗しました。", userName), e.getMessage());
            assertNull(e.getCause());
        }

    }

    /**
     * 008: long getTotp() のテストメソッドです.
     * 秘密鍵復号時にSecretKeyDecryptionExceptionが発生した場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp008() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final String secretKeyStr = "AAAAAAAAA";
        final String secretKeyPass = "PASSWORD";

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = 30L;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                AppConfig.getSecretKeyAttributeName();
                result = SECRET_KEY_ATTR_NAME;
                AppConfig.getSecretKeyPassPhrase();
                result = secretKeyPass;
                SecretKeyDecrypter.decrypt(secretKeyStr, secretKeyPass);
                result = new SecretKeyDecryptionException("秘密鍵の復号化失敗");
            }
        };
        TotpProvider provider = new TotpProviderImpl();

        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals("秘密鍵の復号化に失敗しました。", e.getMessage());
            assertTrue(e.getCause() instanceof SecretKeyDecryptionException);
        }
    }

    /**
     * 009: long getTotp() のテストメソッドです. 復号した秘密鍵が空文字の場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp009() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final long timeStep = 30L;
        final String secretKeyStr = "AAAAAAAAA";
        final String secretKeyPass = "PASSWORD";

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                AppConfig.getSecretKeyAttributeName();
                result = SECRET_KEY_ATTR_NAME;
                AppConfig.getSecretKeyPassPhrase();
                result = secretKeyPass;
                SecretKeyDecrypter.decrypt(secretKeyStr, secretKeyPass);
                result = new byte[] {};
            }
        };
        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals("秘密鍵の復号化に失敗しました。", e.getMessage());
            assertNull(e.getCause());
        }
    }

    /**
     * 010: long getTotp() のテストメソッドです. 復号した秘密鍵がnullの場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp010() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final long timeStep = 30L;
        //final int totpLength = 7;
        final String secretKeyStr = "AAAAAAAAA";
        final String secretKeyPass = "PASSWORD";

        new Expectations() {
            {
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;

                SearchResult searchResult = new SearchResult();
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                AppConfig.getSecretKeyAttributeName();
                result = SECRET_KEY_ATTR_NAME;
                AppConfig.getSecretKeyPassPhrase();
                result = secretKeyPass;
                SecretKeyDecrypter.decrypt(secretKeyStr, secretKeyPass);
                result = null;

                AppConfig.getTimeStepSec();
                result = timeStep;
                //AppConfig.getTotpLength();
                //result = totpLength;
            }
        };
        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals("秘密鍵の復号化に失敗しました。", e.getMessage());
            assertNull(e.getCause());
        }
    }

    /**
     * 011: long getTotp() のテストメソッドです. 復号した秘密鍵がnullの場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp011() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final long timeStep = 30L;
        final int totpLength = 7;
        final String secretKeyStr = "AAAAAAAAA";
        final String secretKeyPass = "PASSWORD";
        final byte[] hash = { 117, -92 };

        new Expectations() {
            {
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;

                SearchResult searchResult = new SearchResult();
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                AppConfig.getSecretKeyAttributeName();
                result = SECRET_KEY_ATTR_NAME;
                AppConfig.getSecretKeyPassPhrase();
                result = secretKeyPass;
                SecretKeyDecrypter.decrypt(secretKeyStr, secretKeyPass);
                result = hash;

                AppConfig.getTimeStepSec();
                result = timeStep;
                AppConfig.getTotpLength();
                result = totpLength;
                _totpGenerator.generateTotp();
                result = new GeneralSecurityException();
            }
        };
        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertTrue(e.getCause() instanceof GeneralSecurityException);
        }
    }

    /**
     * 012: long getTotp() のテストメソッドです.
     * 秘密鍵取得時にUnexpectedExceptionが発生した場合、TotpGenerationExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp012() throws Exception {

        final String userName = "user001";
        final long timeStep = 30L;
        final String secretKeyStr = "AAAAAAAAA";

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = new UnexpectedException("予期せぬエラーが発生しました。");
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        try {
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("ユーザ名：{0}のユーザ情報取得時に予期せぬエラーが発生しました。", userName), e.getMessage());
            assertTrue(e.getCause() instanceof UnexpectedException);
        }
    }

    /**
     * 013: long getTotp() のテストメソッドです. 対象ユーザ名、タイムカウントのTotpが既にキャッシュされている場合 Totpオブジェクトを生成しないことをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp013() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final long timeStep = 30L;
        final int totpLength = 7;
        final String secretKeyStr = "AAAAAAAAA";
        final String secretKeyPass = "PASSWORD";
        final byte[] hash = { 117, -92 };
        final String otp = "123456";
        final long now = System.currentTimeMillis();
        final long timeCount = now / (timeStep * 1000);

        new Expectations() {
            {
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                AppConfig.getSecretKeyAttributeName();
                result = SECRET_KEY_ATTR_NAME;

                // 秘密鍵の復号化
                AppConfig.getSecretKeyPassPhrase();
                result = secretKeyPass;
                SecretKeyDecrypter.decrypt(secretKeyStr, secretKeyPass);
                result = hash;
                // _totpCounter.getTimeCount(); result = timeCount;

                AppConfig.getTimeStepSec();
                result = timeStep;
                AppConfig.getTotpLength();
                result = totpLength;
                _totpGenerator = new TotpGenerator(hash, timeCount, totpLength);
                _totpGenerator.generateTotp();
                result = otp;
                AppConfig.getAllowedTimeCountOffset();
                result = 1L;
            }
        };

        // Totpをキャッシュに追加
        TotpCache totpCache = TotpCache.getInstance();
        Totp expected = new Totp(immutableUserId, timeCount, otp);
        Totp prevTotp = new Totp(immutableUserId, timeCount - 1, otp);
        totpCache.add(expected.getImmutableUserId(), expected);
        totpCache.add(prevTotp.getImmutableUserId(), prevTotp);

        TotpProvider provider = new TotpProviderImpl();
        Totp actual = provider.getTotp(userName);

        assertEquals(timeCount, actual.getTimeCounter());
        assertEquals(otp, actual.getTotp());
        assertEquals(expected.toString(), actual.toString()); // 同一インスタンスであることを確認
    }

    /**
     * 014: long getTotp() のテストメソッドです. 対象ユーザ名、タイムカウントのTotpがキャッシュされていない場合 Totpオブジェクトが生成されることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp014() throws Exception {

        final String userName = "user001";
        final String immutableUserId = "001";
        final long timeStep = 30L;
        final int totpLength = 7;
        final String secretKeyStr = "AAAAAAAAA";
        final String secretKeyPass = "PASSWORD";
        final byte[] hash = { 117, -92 };
        final String otp = "123456";
        final long now = System.currentTimeMillis();
        final long timeCount = now / (timeStep * 1000);

        new Expectations() {
            {
                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, immutableUserId);
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                AppConfig.getSecretKeyAttributeName();
                result = SECRET_KEY_ATTR_NAME;

                // 秘密鍵の復号化
                AppConfig.getSecretKeyPassPhrase();
                result = secretKeyPass;
                SecretKeyDecrypter.decrypt(secretKeyStr, secretKeyPass);
                result = hash;
                // _totpCounter.getTimeCount(); result = timeCount;

                AppConfig.getTimeStepSec();
                result = timeStep;
                AppConfig.getTotpLength();
                result = totpLength;
                _totpGenerator = new TotpGenerator(hash, timeCount, totpLength);
                _totpGenerator.generateTotp();
                result = otp;
                AppConfig.getAllowedTimeCountOffset();
                result = 1L;
            }
        };

        TotpProvider provider = new TotpProviderImpl();
        Totp actual = provider.getTotp(userName);

        assertEquals(timeCount, actual.getTimeCounter());
        assertEquals(otp, actual.getTotp());
    }

    /**
     * 015: long getTotp() のテストメソッドです.
     * 取得したユーザ情報のユーザ固有IDがnullの場合、InvalidUserDataExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp015() throws Exception {

        final String userName = "user001";
        final long timeStep = 30L;
        final String secretKeyStr = "AAAAAAAAA";

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;

                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, null);
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                //AppConfig.getSecretKeyAttributeName();
                //result = SECRET_KEY_ATTR_NAME;
            }
        };

        try {
            TotpProvider provider = new TotpProviderImpl();
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("ユーザ名 ：{0}のユーザ固有ID (データ属性名：{1})が取得できませんでした。", userName,
                    IMMUTABLE_USER_ID_ATTR_NAME), e.getMessage());
        }
    }

    /**
     * 015: long getTotp() のテストメソッドです.
     * 取得したユーザ情報のユーザ固有IDが空文字の場合、InvalidUserDataExceptionがthrowされることをテストします。
     * @throws Exception 予期せぬエラーが発生した場合
     */
    @Test
    public void testGetTotp016() throws Exception {

        final String userName = "user001";
        final long timeStep = 30L;
        final String secretKeyStr = "AAAAAAAAA";

        new Expectations() {
            {
                AppConfig.getTimeStepSec();
                result = timeStep;

                // DAO戻り値設定 (秘密鍵の取得)
                DaoFactory.getInstance();
                result = _daoFactory;
                //AppConfig.getDaoImplClassName();
                //result = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
                _daoFactory.createInstance();
                result = _dao;
                SearchResult searchResult = new SearchResult();
                searchResult.add(SECRET_KEY_ATTR_NAME, secretKeyStr);
                searchResult.add(IMMUTABLE_USER_ID_ATTR_NAME, "");
                _dao.getUser(userName);
                result = searchResult;
                AppConfig.getImmutableUserIdAttributeName();
                result = IMMUTABLE_USER_ID_ATTR_NAME;
                //AppConfig.getSecretKeyAttributeName();
                //result = SECRET_KEY_ATTR_NAME;
            }
        };

        try {
            TotpProvider provider = new TotpProviderImpl();
            provider.getTotp(userName);
            fail("例外が発生するはず");
        } catch (TotpGenerationException e) {
            assertEquals(MessageFormat.format("ユーザ名 ：{0}のユーザ固有ID (データ属性名：{1})が取得できませんでした。", userName,
                    IMMUTABLE_USER_ID_ATTR_NAME), e.getMessage());
        }
    }
}
