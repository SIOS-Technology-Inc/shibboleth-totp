/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.datasource.dao;

import com.sios.idp.shibboleth.dto.SearchResult;
import com.sios.idp.shibboleth.exception.DataAccessException;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;

/**
 * Ldap Data Action Object インターフェースクラスです.
 * @author SIOS Technology, Inc.
 */
public interface Dao {
    /**
     * LDAPからuserNameをベースに情報の取得を行なうメソッドです.
     * @param userName サーチに利用するフィルタの値
     * @return LdapSearchResultオブジェクト
     * @throws DataAccessException LDAPとの接続に失敗した場合に例外が投げられる
     * @throws UserDuplicatedException ユーザ情報の取得で予期せぬエラーが発生している場合に例外が投げられる
     * @throws UnexpectedException 予期せぬエラーが発生した場合に投げられる例外です
     */
    SearchResult getUser(String userName)
            throws DataAccessException, UserDuplicatedException, UnexpectedException;
}
