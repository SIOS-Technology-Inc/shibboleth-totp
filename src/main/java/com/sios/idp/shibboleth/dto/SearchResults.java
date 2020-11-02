/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 任意データソースの検索結果を格納します.
 * @author SIOS Technology, Inc.
 */
public class SearchResults implements Iterable<SearchResult> {
    /** SearchResultを格納するためのListを宣言します. */
    private List<SearchResult> listResults = new ArrayList<SearchResult>();

    /**
     * SearchResultを格納するためのメソッドです.
     * @param lresult SearchResultオブジェクト
     */
    public void add(SearchResult lresult) {
        this.listResults.add(lresult);
    }

    /** SearchResultsのコンストラクタです. */
    public SearchResults() {
    }

    /**
     * 格納されたSearchResultをカウントするためのメソッドです.
     * @return counter SearchResultの数
     */
    public int getCount() {
        int counter = 0;
        counter = listResults.size();
        return counter;
    }

    /**
     * iteratorメソッドの実装の実装を行ないます.
     * @return SearchResultsIterator
     */
    public Iterator<SearchResult> iterator() {
        return new SearchResultsIterator(listResults);
    }

    /**
     * SearchResultsのイテレータです.{@link java.util.Iterator}を実装したイテレータクラスです.
     * @author SIOS Technology, Inc.
     */
    class SearchResultsIterator implements Iterator<SearchResult> {
        /** SearchResultを格納されるためのListの準備を行ないます.　 */
        private List<SearchResult> listResult = new ArrayList<SearchResult>();

        /** counterの値を初期化します. */
        private int counter = 0;

        /**
         * Iteratorで回すための情報を格納するためのコンストラクタです.
         * @param listresult SearchResultが格納されたList
         */
        public SearchResultsIterator(List<SearchResult> listresult) {
            this.listResult = listresult;
        }

        @Override
        /**
         * iteratorのhasNextの実装です.
         * counterの値がlistResult以下である場合はtrueを返却し、
         * listResult以上である場合はfalseを返却します.
         * @return bresult
         */
        public boolean hasNext() {
            boolean bresult = false;
            if (counter < listResult.size()) {
                bresult = true;
            }
            return bresult;
        }

        @Override
        /**
         * iteratorのnextの実装です.
         * counterをインクリメントし,
         * listのcounter-1番目の値を返却します.
         * @return SearchResult
         */
        public SearchResult next() {
            counter++;
            return this.listResult.get(counter - 1);
        }

        @Override
        public void remove() {
        }
    }
}
