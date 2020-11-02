/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.datasource.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.common.AppConfig;

/**
 * LDAPとの接続を担うクラスです. 接続情報を呼び出しもとクラスへ返却します.
 * @author SIOS Technology, Inc.
 */
public class LdapConnectionImpl implements LdapConnection {
    /** loggerを呼び出します. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** DirContext用変数を定義します. */
    private DirContext ctx;

    @Override
    /**
     * LDAPとの接続を行なうためのメソッドです。
     * 例外発生時（接続情報が間違っている場合）には、NamingExceptionを投げます.
     * @throws NamingException LDAPの操作に失敗した際に投げられる例外クラスです.
     */
    public void open() throws NamingException {
        logger.debug("LDAPとの接続を開始します。");
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, AppConfig.getLdapUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, AppConfig.getLdapBindDN());
        env.put(Context.SECURITY_CREDENTIALS, AppConfig.getLdapBindPassword());
        this.ctx = new InitialDirContext(env);
        logger.debug("LDAPとの接続が完了しました。");
    }

    /**
     * LDAPへ検索を実行するためのメソッドです.
     * @param filter 検索に利用するBaseDN
     * @param expr 検索に利用するLDAPQuery
     * @param scontrols 検索範囲
     * @param args exprをプレースホルダ―として各パラメータに渡される値
     * @return {@literal NamingEnumeration<SearchResult>}
     * @throws NamingException LDAPの操作に失敗した際に投げられる例外クラスです.
     */
    public NamingEnumeration<SearchResult> search(String filter, String expr,
            SearchControls scontrols, String...args) throws NamingException {
        NamingEnumeration<SearchResult> searchResults = null;
        DirContext dircontext = this.ctx;
        searchResults = dircontext.search(filter, expr, args, scontrols);
        return searchResults;
    }

    /**
     * Ldapのコネクションをクローズするためのメソッドです.
     * @throws NamingException LDAPの操作に失敗した際に投げられる例外クラスです.
     */
    public void close() throws NamingException {
        if (this.ctx != null) {
            try {
                this.ctx.close();
            } catch (NamingException ne) {
                throw ne;
            }
            this.ctx = null;
        }
    }
}
