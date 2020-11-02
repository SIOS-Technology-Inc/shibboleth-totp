/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.datasource.dao;

import static org.junit.Assert.*;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;

import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.exception.DaoInstantiationException;

/**
 * {@link com.sios.idp.shibboleth.datasource.dao.DaoFactory}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class DaoFactoryTest {

    DaoFactory target = DaoFactory.getInstance();
    @Mocked
    final AppConfig _appConfig = null;

    /**
     * 001: DaoFactory getInstance() のテストメソッドです. 取得されるインスタンスがSingletonであることを確認する。
     */
    @Test
    public void testGetInstance() {
        DaoFactory factory = DaoFactory.getInstance();
        assertTrue(target.equals(factory));
        assertTrue(target == factory);
    }

    /**
     * 002: Dao createInstance() のテストメソッドです. 例外が発生せずにDao実装クラスがインスタンス化されることをテストします。
     */
    @Test
    public void testCreateInstance001() {

        final String expected = "com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl";
        new Expectations() {
            {
                AppConfig.getDaoImplClassName();
                result = expected;
            }
        };
        Dao dao = null;
        try {
            dao = target.createInstance();
        } catch (DaoInstantiationException e) {
            fail();
        }
        assertEquals(expected, dao.getClass().getName());
    }

    /**
     * 003: Dao createInstance() のテストメソッドです.
     * 存在しないクラス名が指定された場合、DaoInstantiationExceptionがthrowされることをテストします。
     * @throws DaoInstantiationException DAOのインスタンス生成に失敗した場合
     */
    @Test(expected = DaoInstantiationException.class)
    public void testCreateInstance002() throws DaoInstantiationException {

        final String invalidDaoClassName = "InvalidLdapDaoImpl";
        new Expectations() {
            {
                AppConfig.getDaoImplClassName();
                result = invalidDaoClassName;
            }
        };
        target.createInstance();
        fail("例外が発生するはず");
    }

}
