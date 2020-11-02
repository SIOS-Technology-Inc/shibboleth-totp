/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.dto;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * SearchResultクラスの単体テストを行うためのクラスです.
 * @author SIOS Technology, Inc.
 */
public class SearchResultTest {

    /**
     * TestNo ○- SearchResultのprivate変数にaddメソッド経由で値が格納される事を確認するためのテストです.
     * また、当テスト該当クラスに作成された全てのメソッドの正常テストとする （addメソッドでデータが格納されている事を確認するために全てのメソッドを呼び出しているため）.
     */
    @Test
    public void add() {
        final String key = "test_key";
        final String key2 = "test_key2";
        final String value = "test_value";
        final String value2 = "test_value2";
        SearchResult result = new SearchResult();
        result.add(key, value);
        result.add(key2, value2);
        String resultString = result.getValue(key);
        assertEquals(value, resultString);
    }

    /**
     * TestNo ○- SearchResultのprivate変数にaddメソッド経由で値が格納される事を確認するためのテストです。 上記との違いはブランク（String
     * ""）がkey設定となっている場合に正常に格納し、 取得が行える事を確認します.
     *
     */
    @Test
    public void blankadd() {
        final String key = "test_key";
        final String blankKey = "";
        final String value = "false_value";
        final String value2 = "true_value";
        SearchResult result = new SearchResult();
        result.add(key, value);
        result.add(blankKey, value2);
        String reultString = result.getValue(blankKey);
        assertEquals(value2, reultString);
    }

    /**
     * TestNo ○- SearchResult内のaddへ記号が含まれていた場合でも問題無くhashMapへ格納され、 値が引ける事を確認する.
     *
     */
    @Test
    public void symboladd() {
        final String key = "!#$%&'()=~|@[;:],./";
        final String value = "\"@[;:]./\\\"";
        SearchResult result = new SearchResult();
        result.add(key, value);
        String resultString = result.getValue(key);
        assertEquals(value, resultString);
    }

    /**
     * valueの中にjsが記述されていても問題なくhashMapへ格納され、 値が引ける事を確認する.
     *
     */
    @Test
    public void javascriptadd() {
        final String key = "js";
        final String value = "<script>arrte(\"hogehoge\")</script>";
        SearchResult result = new SearchResult();
        result.add(key, value);
        String resultString = result.getValue(key);
        assertEquals(value, resultString);
    }

    /**
     * SearchResult内に値を入れて置き、値を取得する際のkey指定をnullにした場合、 返却される値がなくnullが帰ってくる事を確認する.
     *
     */
    @Test
    public void getValuekeyNull() {
        final String key = "keytest";
        final String value = "testvalues";
        SearchResult result = new SearchResult();
        result.add(key, value);
        String resultString = result.getValue(null);
        assertNull(resultString);
    }

    /**
     * SearchResult内のhashMapに何も格納されていない場合にnullが帰ってくる事を確認する.
     *
     */
    @Test
    public void getValuemapNull() {
        SearchResult result = new SearchResult();
        String resultString = result.getValue("testkey");
        assertNull(resultString);
    }
}
