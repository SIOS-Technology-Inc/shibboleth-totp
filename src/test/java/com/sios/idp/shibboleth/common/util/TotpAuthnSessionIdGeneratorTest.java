/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.datasource.dao.Dao;
import com.sios.idp.shibboleth.datasource.dao.DaoFactory;
import com.sios.idp.shibboleth.dto.SearchResult;
import com.sios.idp.shibboleth.exception.DaoInstantiationException;
import com.sios.idp.shibboleth.exception.DataAccessException;
import com.sios.idp.shibboleth.exception.TotpAuthnSessionIdGenerationException;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;

import mockit.Mocked;
import mockit.Expectations;

/**
 * {@link com.sios.idp.shibboleth.common.util.TotpAuthnSessionIdGenerator}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class TotpAuthnSessionIdGeneratorTest {

    @Mocked
    private DaoFactory mockDaoFactory;

    @Mocked
    private Dao mockDao;

    @Mocked
    private SearchResult mockResult;

    @Mocked
    private AppConfig appConfig;

    /**
     * 通常の場合に正しくTOTP認証セッションIDが生成できることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     * @throws UserDuplicatedException ユーザが複数存在する場合の例外
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void generateTotpAuthnSessionIdNormal() throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = mockDao;
                mockDao.getUser(anyString);
                result = mockResult;
                mockResult.getValue("gAuth");
                result = "secretKey";
                AppConfig.getSecretKeyAttributeName();
                result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
            }
        };

        try {
            final String sessionId = TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            assertEquals("r3a1QMS8oHF9NST17Qo3bhKnlHrC8vV8uxxrHiF3IKw=", sessionId);
        } catch (TotpAuthnSessionIdGenerationException e) {
            fail("例外が発生したためテストNG");
        }
    }

    /**
     * Daoクラス初期化時にエラーが発生した場合に正しい例外がthrowされることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     */
    @Test
    public void generateTotpAuthnSessionIdDaoInstantiationError() throws DaoInstantiationException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = new DaoInstantiationException();
                //AppConfig.getSecretKeyAttributeName();
                //result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
            }
        };

        try {
            TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            fail("例外が発生しないためテストNG");
        } catch (TotpAuthnSessionIdGenerationException e) {
            assertEquals("ユーザーのTOTP秘密鍵の取得に失敗しました。", e.getMessage());
            assertTrue(e.getCause() instanceof DaoInstantiationException);
        }
    }

    /**
     * ユーザ検索中に接続エラーが発生した場合に正しい例外がthrowされることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     * @throws UserDuplicatedException ユーザが複数存在する場合の例外
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void generateTotpAuthnSessionIdDataAccessError() throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = mockDao;
                mockDao.getUser(anyString);
                result = new DataAccessException();
                //AppConfig.getSecretKeyAttributeName();
                //result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
            }
        };

        try {
            TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            fail("例外が発生しないためテストNG");
        } catch (TotpAuthnSessionIdGenerationException e) {
            assertEquals("ユーザーのTOTP秘密鍵の取得に失敗しました。", e.getMessage());
            assertTrue(e.getCause() instanceof DataAccessException);
        }
    }

    /**
     * ユーザ検索結果が複数存在するエラーが発生した場合に正しい例外がthrowされることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     * @throws UserDuplicatedException ユーザが複数存在する場合の例外
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void generateTotpAuthnSessionIdUserDuplicated() throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = mockDao;
                mockDao.getUser(anyString);
                result = new UserDuplicatedException();
                //AppConfig.getSecretKeyAttributeName();
                //result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
            }
        };

        try {
            TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            fail("例外が発生しないためテストNG");
        } catch (TotpAuthnSessionIdGenerationException e) {
            assertEquals("ユーザーのTOTP秘密鍵の取得に失敗しました。", e.getMessage());
            assertTrue(e.getCause() instanceof UserDuplicatedException);
        }
    }

    /**
     * ユーザ検索中に予期せぬエラーが発生した場合に正しい例外がthrowされることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     * @throws UserDuplicatedException ユーザが複数存在する場合の例外
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void generateTotpAuthnSessionIdUnexpectedError() throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = mockDao;
                mockDao.getUser(anyString);
                result = new UnexpectedException();
                //AppConfig.getSecretKeyAttributeName();
                //result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
            }
        };

        try {
            TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            fail("例外が発生しないためテストNG");
        } catch (TotpAuthnSessionIdGenerationException e) {
            assertEquals("ユーザーのTOTP秘密鍵の取得に失敗しました。", e.getMessage());
            assertTrue(e.getCause() instanceof UnexpectedException);
        }
    }

    /**
     * ユーザ検索結果が0件だった場合に正しい例外がthrowされることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     * @throws UserDuplicatedException ユーザが複数存在する場合の例外
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void generateTotpAuthnSessionIdUserNotFound() throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = mockDao;
                mockDao.getUser(anyString);
                result = null;
                //AppConfig.getSecretKeyAttributeName();
                //result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
            }
        };

        try {
            TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            fail("例外が発生しないためテストNG");
        } catch (TotpAuthnSessionIdGenerationException e) {
            assertEquals("ユーザーのTOTP秘密鍵の取得に失敗しました。", e.getMessage());
            Throwable cause = e.getCause();
            assertTrue(cause instanceof TotpAuthnSessionIdGenerationException);
            assertEquals("ユーザー名usernameの検索結果が0件です。", cause.getMessage());
        }
    }

    /**
     * ユーザのTOTP秘密鍵が設定されていなかった場合に正しい例外がthrowされることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     * @throws UserDuplicatedException ユーザが複数存在する場合の例外
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     */
    @Test
    public void generateTotpAuthnSessionIdSecretKeyNotDefined() throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = mockDao;
                mockDao.getUser(anyString);
                result = mockResult;
                mockResult.getValue("gAuth");
                result = null;
                AppConfig.getSecretKeyAttributeName();
                result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
            }
        };

        try {
            TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            fail("例外が発生しないためテストNG");
        } catch (TotpAuthnSessionIdGenerationException e) {
            assertEquals("ユーザーのTOTP秘密鍵の取得に失敗しました。", e.getMessage());
            Throwable cause = e.getCause();
            assertTrue(cause instanceof TotpAuthnSessionIdGenerationException);
            assertEquals("ユーザーusernameにTOTP秘密鍵が設定されていません。", cause.getMessage());
        }
    }

    /**
     * ハッシュ化アルゴリズムが利用できない場合に正しい例外がthrowされることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     * @throws UserDuplicatedException ユーザが複数存在する場合の例外
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     * @throws NoSuchAlgorithmException ハッシュ化アルゴリズムが利用できない場合の例外
     * @throws UnsupportedEncodingException エンコードが利用できない場合の例外
     */
    @Test
    //public void generateTotpAuthnSessionIdNoSuchAlgorithm() throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException, NoSuchAlgorithmException, UnsupportedEncodingException {
    public void generateTotpAuthnSessionIdNoSuchAlgorithm(@Mocked Sha256 sha256) throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException, NoSuchAlgorithmException, UnsupportedEncodingException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class, Sha256.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = mockDao;
                mockDao.getUser(anyString);
                result = mockResult;
                mockResult.getValue("gAuth");
                result = "secretKey";
                AppConfig.getSecretKeyAttributeName();
                result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
                Sha256.hash(anyString);
                result = new NoSuchAlgorithmException();
            }
        };

        try {
            TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            fail("例外が発生しないためテストNG");
        } catch (TotpAuthnSessionIdGenerationException e) {
        	assertTrue(e.getMessage().contains("アルゴリズムが利用できません。"));
            assertTrue(e.getCause() instanceof NoSuchAlgorithmException);
        }
    }

    /**
     * エンコードが利用できない場合に正しい例外がthrowされることをテストします.
     * @throws DaoInstantiationException Daoクラスの初期化時の例外
     * @throws UserDuplicatedException ユーザが複数存在する場合の例外
     * @throws DataAccessException データアクセス時の例外
     * @throws UnexpectedException 想定外の例外
     * @throws NoSuchAlgorithmException ハッシュ化アルゴリズムが利用できない場合の例外
     * @throws UnsupportedEncodingException エンコードが利用できない場合の例外
     */
    @Test
    //public void generateTotpAuthnSessionIdUnsupportedEncoding() throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException, NoSuchAlgorithmException, UnsupportedEncodingException {
    public void generateTotpAuthnSessionIdUnsupportedEncoding(@Mocked Sha256 sha256) throws DaoInstantiationException, DataAccessException, UserDuplicatedException, UnexpectedException, NoSuchAlgorithmException, UnsupportedEncodingException {
        final String username = "username";
        final String issueDate = "1234567890";

        //new Expectations(AppConfig.class, Sha256.class) {
        new Expectations() {
            {
                DaoFactory.getInstance();
                result = mockDaoFactory;
                mockDaoFactory.createInstance();
                result = mockDao;
                mockDao.getUser(anyString);
                result = mockResult;
                mockResult.getValue("gAuth");
                result = "secretKey";
                AppConfig.getSecretKeyAttributeName();
                result = "gAuth";
                AppConfig.getTotpAuthnSessionIdSalt();
                result = "Salt";
                Sha256.hash(anyString);
                result = new UnsupportedEncodingException();
            }
        };

        try {
            TotpAuthnSessionIdGenerator.generateTotpAuthnSessionId(username, issueDate);
            fail("例外が発生しないためテストNG");
        } catch (TotpAuthnSessionIdGenerationException e) {
        	assertTrue(e.getMessage().contains("エンコードが利用できません。"));
            assertTrue(e.getCause() instanceof UnsupportedEncodingException);
        }
    }

}
