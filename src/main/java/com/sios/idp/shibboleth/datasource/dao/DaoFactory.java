/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.datasource.dao;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.exception.DaoInstantiationException;

/**
 * {@link com.sios.idp.shibboleth.datasource.dao.Dao}を生成するFactoryクラスです.
 * @author SIOS Technology, Inc.
 */
public final class DaoFactory {

    /** Class logger. */
    private final Logger _logger = LoggerFactory.getLogger(this.getClass());

    /** {@link com.sios.idp.shibboleth.datasource.dao.Dao}を生成するSingletonなFactoryインスタンスです. */
    private static final DaoFactory FACTORY = new DaoFactory();

    /**
     * Singletonなインスタンスを返します.
     * @return {@link com.sios.idp.shibboleth.datasource.dao.Dao}を生成するFactoryクラス
     * */
    public static DaoFactory getInstance() {
        return FACTORY;
    }

    /**
     * {@link com.sios.idp.shibboleth.common.AppConfig}で指定された
     * {@link com.sios.idp.shibboleth.datasource.dao.Dao}のインスタンスを生成します.
     * @return {@link com.sios.idp.shibboleth.datasource.dao.Dao}の実装インスタンス
     * @throws DaoInstantiationException DAOのインスタンス生成に失敗した場合
     */
    public Dao createInstance() throws  DaoInstantiationException {

        String daoClassName = AppConfig.getDaoImplClassName();
        _logger.info(MessageFormat.format("データソースにアクセスするDAOクラス {0} を生成します。", daoClassName));

        Object dao = null;
        try {
            dao = Class.forName(daoClassName).newInstance();
        } catch (Exception e) {
            _logger.error(MessageFormat.format(
                    "データソースにアクセスするDAOクラス {0} の生成に失敗しました。", daoClassName));
            throw new DaoInstantiationException(e);
        }
        return (Dao) dao;
    }

}
