/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common.util;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sios.idp.shibboleth.dto.SearchResults;
import com.sios.idp.shibboleth.exception.UnexpectedException;
import com.sios.idp.shibboleth.exception.UserDuplicatedException;

/**
 * Validationロジッククラスです.
 * @author SIOS Technology, Inc.
 */
public final class ValidationUtil {
    /** loggerの呼び出しを行ないます. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);

    /**
     * ValidateUtilのプライベートコンストラクタです.
     */
    private ValidationUtil() {

    }

    /**
     * LdapSearchResultが格納されたListが0である場合にfalseを返し、 １つだけの場合、trueを返します。
     * 複数のResultが格納されている場合にはユーザ情報重複によりエラーが返されます.
     * @param results LdapSearchResultsオブジェクト
     * @return boolean型を返します.
     * @throws UserDuplicatedException ユーザ情報の取得で予期せぬエラーが発生している場合に例外が投げられる.
     * @throws UnexpectedException 予期せぬエラーが発生した場合に投げられる例外
     */
    public static boolean isSingleRecorde(SearchResults results)
            throws UserDuplicatedException, UnexpectedException {
        int i = results.getCount();
        if (i == 1) {
            return true;
        } else if (i == 0) {
            LOGGER.debug("ユーザ情報が引けませんでした。");
            return false;
        } else if (1 < i) {
            throw new UserDuplicatedException("ユーザ情報が複数存在します。");
        } else {
            throw new UnexpectedException(MessageFormat.format("ユーザ情報の件数が異常です。（ユーザ情報：{0}件）", i));

        }
    }

}
