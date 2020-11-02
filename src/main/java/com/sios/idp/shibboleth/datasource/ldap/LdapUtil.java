/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.datasource.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.AppConfig;
import com.sios.idp.shibboleth.dto.SearchResult;
import com.sios.idp.shibboleth.dto.SearchResults;

/**
 * LdapUtilクラスです. Ldapのsearchを実装するメソッドを持ちます.
 * @author SIOS Technology, Inc.
 */
public final class LdapUtil {
    /** loggerの呼び出しをします. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtil.class);

    /**
     * コンストラクタです.
     * LdapUtilクラス内からしか参照できません.
     */
    private LdapUtil() {
    }

    /**
     * LDAPサーチを利用して必要な情報を取得するためのメソッドです. 上位で受け取ったフィルター情報を元にsearchを実行します.
     * 実行結果についてはConvertResultに渡され、処理が行なわれます.
     * @param userName ユーザネーム
     * @return searchResultオブジェクト
     * @throws NamingException lookup処理に誤りがあります.
     */
    public static SearchResults search(String...userName) throws NamingException {
        LdapConnection lcon = new LdapConnectionImpl();
        NamingEnumeration<javax.naming.directory.SearchResult> results = null;
        try {
            lcon.open();
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = lcon.search(
                    AppConfig.getLdapBaseDN(), AppConfig.getLdapFilter(), constraints, userName);
            return convertResult(results);
        } catch (NamingException ne) {
            throw ne;
        } finally {
            if (results != null) {
                LOGGER.debug("LDAPとの接続切断準備に入ります。");
                results.close();
                LOGGER.debug("LDAPとの接続切断準備が完了しました。");
            }
            if (lcon != null) {
                LOGGER.debug("LDAPとの接続を切断します。");
                lcon.close();
                LOGGER.debug("LDAPとの接続が切断されました。");
            }
        }
    }

    /**
     * 上位で受け取ったsearchの結果をSearchResultのaddMap変数へ詰め替えるための メソッドです.
     * この作業完了後、上位メソッドはctxを利用してsearchした結果とctxをclose処理します.
     * @param result 上位メソッドでsearchした結果です.
     * @return SearchResultsオブジェクト
     * @throws NamingException LDAPの操作に失敗した際に投げられる例外クラスです.
     */
    private static SearchResults convertResult(
            NamingEnumeration<javax.naming.directory.SearchResult> result)
            throws NamingException {
        javax.naming.directory.SearchResult convertResult = null;
        NamingEnumeration<? extends Attribute> attrResult = null;
        SearchResults lresults = new SearchResults();
        SearchResult lresult = new com.sios.idp.shibboleth.dto.SearchResult();
        while (result != null && result.hasMore()) {
            convertResult = result.next();
            attrResult = convertResult.getAttributes().getAll();
            while (attrResult != null && attrResult.hasMore()) {
                Attribute attr = attrResult.next();
                attr.get().toString();
                lresult.add(attr.getID().toString(), attr.get().toString());
            }
            lresults.add(lresult);
        }
        return lresults;
    }
}
