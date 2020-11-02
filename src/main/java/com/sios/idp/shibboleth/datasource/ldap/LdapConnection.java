/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.datasource.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * LdapConnect用のインターフェースです.
 * @author SIOS Technology, Inc.
 */
public interface LdapConnection {
    /**
     * LDAPコネクション用のメソッドです.
     * @throws NamingException LDAPの操作に失敗した際に投げられる例外クラスです.
     */
    void open() throws NamingException;
    /**
     * LDAPへ検索を実行するためのメソッドです.
     * @param filter 検索に利用するBaseDN
     * @param expr 検索に利用するLDAPQuery
     * @param scontrols 検索範囲
     * @param args exprをプレースホルダ―として各パラメータへ渡される値
     * @return {@literal NamingEnumeration<SearchResult>}
     * @throws NamingException LDAPの操作に失敗した際に投げられる例外クラスです.
     */
    NamingEnumeration<SearchResult> search(
            String filter, String expr, SearchControls scontrols, String...args)
            throws NamingException;

    /**
     * Ldapのコネクションをクローズするためのメソッドです.
     * @throws NamingException LDAPの操作に失敗した際に投げられる例外クラスです.
     */
    void close() throws NamingException;

}
