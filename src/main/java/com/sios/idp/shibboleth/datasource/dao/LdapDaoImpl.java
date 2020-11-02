/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.datasource.dao;

import java.text.MessageFormat;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.util.ValidationUtil;
import com.sios.idp.shibboleth.datasource.ldap.LdapUtil;
import com.sios.idp.shibboleth.dto.SearchResult;
import com.sios.idp.shibboleth.dto.SearchResults;
import com.sios.idp.shibboleth.exception.DataAccessException;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;
/**
 * LDAP サーチ、バインドなど実行した結果はresult変数へ格納後、上位へ返します.
 * @author SIOS Technology, Inc.
 */
public class LdapDaoImpl implements Dao {
    /** loggerの呼び出します. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    /**
     * 与えられたuserNameを元にLDAPから情報の取得を行なうメソッドです.
     * @param userName ユーザネーム
     * @return SearchResultオブジェクト
     * @throws DataAccessException Ldapとの接続に失敗した際に投げられる例外です.
     * @throws UserDuplicatedException Ldapの検索の結果問題が発生した際に投げられる例外です.
     */
    public SearchResult getUser(String userName)
            throws DataAccessException, UserDuplicatedException, UnexpectedException {
        logger.debug(MessageFormat.format("{0} の情報取得を開始します。", userName));
        SearchResults results = null;
        try {
            results = LdapUtil.search(userName);
        } catch (NamingException ne) {
            throw new DataAccessException(ne);
        }
        boolean validate = ValidationUtil.isSingleRecorde(results);
        if (validate) {
            logger.debug("ユーザ情報の取得が完了しました。");
            return results.iterator().next();
        } else {
            logger.debug("ユーザ情報の取得に失敗しました。");
            return null;
        }
    }

}
