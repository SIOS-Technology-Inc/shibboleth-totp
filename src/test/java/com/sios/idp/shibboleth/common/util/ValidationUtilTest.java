/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import static org.junit.Assert.*;
import mockit.Mocked;
import mockit.Expectations;

import org.junit.Test;

import com.sios.idp.shibboleth.dto.SearchResults;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;

/**
 * VlidetionUtilの単体テストクラスです..
 * @author SIOS Technology, Inc.
 */
public class ValidationUtilTest {
    /**
     * SearchResultのモック.
     */
    @Mocked
    private SearchResults searchResults;

    /**
     * 001 validationUtilの正常処理その1 正常に取得する事ができ、Trueを返却します.
     * @throws UserDuplicatedException 検索の結果が不正である場合に投げられる
     * @throws UnexpectedException 予期せぬエラーが発生した場合に投げられる
     */
    @Test
    public void validation() throws UserDuplicatedException, UnexpectedException {
        SearchResults result = new SearchResults();
        new Expectations() {
            {
                searchResults.getCount();
                result = 1;
            }
        };
        assertTrue(ValidationUtil.isSingleRecorde(result));
    }

    /**
     * 002 validationUtilの正常処理その2 正常に値が取得出来、Falseを返却します.
     * @throws UserDuplicatedException 検索の結果が不正である場合に投げられる
     * @throws UnexpectedException 予期せぬエラーが発生した場合に投げられる
     */
    @Test
    public void validationZero() throws UserDuplicatedException, UnexpectedException {
        SearchResults result = new SearchResults();
        new Expectations() {
            {
                searchResults.getCount();
                result = 0;
            }
        };
        assertFalse(ValidationUtil.isSingleRecorde(result));
    }

    /**
     * 003 validationUtilの異常処理 検索結果が複数件入った場合に例外が投げられる事を確認します.
     * @throws UserDuplicatedException 検索の結果が不正である場合に投げられる
     * @throws UnexpectedException 予期せぬエラーが発生した場合に投げられる
     */
    @Test(expected = UserDuplicatedException.class)
    public void validationDuplication() throws UserDuplicatedException, UnexpectedException {
        SearchResults result = new SearchResults();
        new Expectations() {
            {
                searchResults.getCount();
                result = 3;
            }
        };
        assertTrue(ValidationUtil.isSingleRecorde(result));
    }

    /**
     * 004 validationUtilの異常処理 カウントの結果がマイナス値となった場合に例外が投げられる事を確認します.
     * @throws UserDuplicatedException 検索の結果が不正である場合に投げられる
     * @throws UnexpectedException 予期せぬエラーが発生した場合に投げられる
     */
    @Test(expected = UnexpectedException.class)
    public void validationUnexpected() throws UserDuplicatedException, UnexpectedException {
        SearchResults result = new SearchResults();
        new Expectations() {
            {
                searchResults.getCount();
                result = -1;
            }
        };
        assertTrue(ValidationUtil.isSingleRecorde(result));
    }
}
