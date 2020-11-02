/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.dto;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

/**
 * SearchResultsクラスの単体テスト用クラスです.
 * @author SIOS Technology, Inc.
 */
public class SearchResultsTest {

    /**
     * 001 SearchResultを格納するメソッドの正常処理メソッドです. オブジェクトが格納されている事を確認します.
     */
    @Test
    public void addMethod() {
        SearchResults searchResults = new SearchResults();
        SearchResult result = new SearchResult();
        searchResults.add(result);
        assertTrue(searchResults.iterator().hasNext());
    }

    /**
     * 002 格納されているSearchResultオブジェクトの数をカウントし、 その結果を返却するためのメソッドが正常処理される事を確認するためのメソッド.
     */
    @Test
    public void countMethod() {
        SearchResults searchResults = new SearchResults();
        final int trueNum = 3;
        for (int i = 0; i < trueNum; i++) {
            SearchResult result = new SearchResult();
            searchResults.add(result);
        }
        assertEquals(searchResults.getCount(), trueNum);
    }

    /**
     * 003 イテレータのhasNext実装に関する正常系処理テストです. 対象のリストの中身が空もしくは続きがないため、falseが返却される事を期待します.
     */
    @Test
    public void iteratorhasNextFalseMethod() {
        SearchResults searchResults = new SearchResults();
        assertFalse(searchResults.iterator().hasNext());
    }

    /**
     * 004 イテレータのhasNext実装に関する正常系処理テストです。 対象のリスト中身に続きがある場合にTrueが返却される事を期待します.
     *
     */
    @Test
    public void iteratorhasNextTrueMethod() {
        SearchResults searchResults = new SearchResults();
        for (int i = 0; i < 3; i++) {
            SearchResult searchResult = new SearchResult();
            searchResults.add(searchResult);
        }
        assertTrue(searchResults.iterator().hasNext());

    }

    /**
     * 005 イテレータのnext実装に関する異常系処理テストです。 対象のリストの中身が無い状態でnextが呼ばれた際に例外が発生する事を確認します.
     *
     */
    @Test(expected = Exception.class)
    public void iteratorNextFalse() {
        SearchResults searchResults = new SearchResults();
        searchResults.iterator().next();
    }

    /**
     * 006 イテレータのnext実装に関する正常系処理テストです 対象のリストの中身が追記されている状態でnextが呼ばれた際に格納された次のオブジェクトが呼ばれている事を確認します.
     *
     */
    @Test
    public void iteratorNextTrue() {
        SearchResults searchResults = new SearchResults();
        SearchResult searchResult = new SearchResult();
        searchResults.add(searchResult);
        assertNotNull(searchResults.iterator().next());
    }

    /**
     * 007 イテレータのnextとhashNextの複合確認テストです 最初にTrueが返却され、最後にFlaseが返却される事を確認します.
     *
     */
    @Test
    public void iteratorNextHasNext() {
        SearchResults searchResults = new SearchResults();
        SearchResult searchResult = new SearchResult();
        searchResults.add(searchResult);
        searchResults.add(searchResult);
        Iterator<SearchResult> iter = null;
        for (iter = searchResults.iterator(); iter.hasNext();) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        assertFalse(iter.hasNext());
    }
}
