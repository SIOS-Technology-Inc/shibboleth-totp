/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
// CHECKSTYLE:OFF
package com.sios.idp.shibboleth.common;

import static org.junit.Assert.*;

import javax.servlet.ServletContext;

import org.junit.Test;

import com.sios.idp.shibboleth.exception.ConfigInitializationException;

import mockit.Expectations;
import mockit.Mocked;

/**
 * {@link com.sios.idp.shibboleth.common.AppConfig}のテストクラスです.
 * @author SIOS Technology, Inc.
 */
public class AppConfigTest {

    @Mocked
    ServletContext ctx;

    /**
     * 001: AppConfigの全getterのテストメソッドです.
     * ロード済みのデータが取得できることをテストします。データをロードするAppConfigLoaderはモックを使用しないため、一部AppConfigのテストも含みます。
     * @throws ConfigInitializationException コンテキストパラメータのロードに失敗した場合
     */
    @Test
    public void testHandle001() throws ConfigInitializationException {

        final String expLdapUrl = "ldapUrlValue";
        final String expLdapBindDN = "ldapBaindDNValue";
        final String expLdapBindPassword = "ldapBindPasswordValue";
        final String expLdapBaseDN = "ldapBaseDNValue";
        final String expLdapFilter = "ldapFilterValue";
        final String expCipherArgorithm = "cipherArgorithmVallue";
        final Integer expSaltLength = Integer.valueOf(1);
        final Integer expIterationCount = Integer.valueOf(2);
        final Integer expCipherKeyLength = Integer.valueOf(3);
        final String expCipherTransformationName = "cipherTransformationNameValue";
        final String expImmutableUserIdAttributeName = "immutableIdAttributeNameValue";
        final String expSecretKeyAttributeName = "secretKeyAttributeNameValue";
        final String expSecretKeyPassPhrase = "secretKeyPassPhraseValule";
        final String expDaoImplClassName = "expDaoImplClassNameValue";
        final Long expTimeStepSec = 30L;
        final Integer expTotpLength = 6;
        final Integer expAllowedTimeCountOffset = 1;
        final Long totpCacheExpirationBufferSec = 30L;
        final String expTotpAuthnSessionIdCookieName = "totpAuthnSessionIdCookieNameValue";
        final String expTotpAuthnSessionIssueDateCookieName = "totpAuthnSessionIssueDateCookieNameValue";
        final Integer expTotpAuthnSessionExpirationSec = Integer.valueOf(60);
        final String expTotpAuthnSessionIdSalt = "totpAuthnSessionIdSalttValue";

        new Expectations() {
            {
                ctx.getInitParameter("ldapUrl");
                result = expLdapUrl;
                ctx.getInitParameter("ldapBindDN");
                result = expLdapBindDN;
                ctx.getInitParameter("ldapBindPassword");
                result = expLdapBindPassword;
                ctx.getInitParameter("ldapBaseDN");
                result = expLdapBaseDN;
                ctx.getInitParameter("ldapFilter");
                result = expLdapFilter;
                ctx.getInitParameter("cipherArgorithm");
                result = expCipherArgorithm;
                ctx.getInitParameter("saltLength");
                result = expSaltLength;
                ctx.getInitParameter("iterationCount");
                result = expIterationCount;
                ctx.getInitParameter("cipherKeyLength");
                result = expCipherKeyLength;
                ctx.getInitParameter("cipherTransformationName");
                result = expCipherTransformationName;
                ctx.getInitParameter("immutableIdAttributeName");
                result = expImmutableUserIdAttributeName;
                ctx.getInitParameter("secretKeyAttributeName");
                result = expSecretKeyAttributeName;
                ctx.getInitParameter("secretKeyPassPhrase");
                result = expSecretKeyPassPhrase;
                ctx.getInitParameter("daoImplClassName");
                result = expDaoImplClassName;
                ctx.getInitParameter("timeStepSec");
                result = expTimeStepSec;
                ctx.getInitParameter("totpLength");
                result = expTotpLength;
                ctx.getInitParameter("allowedTimeCountOffset");
                result = expAllowedTimeCountOffset;
                ctx.getInitParameter("totpCacheExpirationBufferSec");
                result = totpCacheExpirationBufferSec;
                ctx.getInitParameter("totpAuthnSessionIdCookieName");
                result = expTotpAuthnSessionIdCookieName;
                ctx.getInitParameter("totpAuthnSessionIssueDateCookieName");
                result = expTotpAuthnSessionIssueDateCookieName;
                ctx.getInitParameter("totpAuthnSessionExpirationSec");
                result = expTotpAuthnSessionExpirationSec;
                ctx.getInitParameter("totpAuthnSessionIdSalt");
                result = expTotpAuthnSessionIdSalt;
            }
        };

        AppConfigLoader loader = new AppConfigLoader(ctx);
        loader.load(AppConfig.class);

        assertEquals(expLdapUrl, AppConfig.getLdapUrl());
        assertEquals(expLdapBindDN, AppConfig.getLdapBindDN());
        assertEquals(expLdapBindPassword, AppConfig.getLdapBindPassword());
        assertEquals(expLdapBaseDN, AppConfig.getLdapBaseDN());
        assertEquals(expLdapFilter, AppConfig.getLdapFilter());
        assertEquals(expCipherArgorithm, AppConfig.getCipherArgorithm());
        assertEquals(expSaltLength, AppConfig.getSaltLength());
        assertEquals(expIterationCount, AppConfig.getIterationCount());
        assertEquals(expCipherKeyLength, AppConfig.getCipherKeyLength());
        assertEquals(expCipherTransformationName, AppConfig.getCipherTransformationName());
        assertEquals(expImmutableUserIdAttributeName, AppConfig.getImmutableUserIdAttributeName());
        assertEquals(expSecretKeyAttributeName, AppConfig.getSecretKeyAttributeName());
        assertEquals(expSecretKeyPassPhrase, AppConfig.getSecretKeyPassPhrase());
        assertEquals(expDaoImplClassName, AppConfig.getDaoImplClassName());
        assertEquals(expTimeStepSec, AppConfig.getTimeStepSec());
        assertEquals(expTotpLength, AppConfig.getTotpLength());
        assertEquals(expAllowedTimeCountOffset, AppConfig.getAllowedTimeCountOffset());
        assertEquals(totpCacheExpirationBufferSec, AppConfig.getTotpCacheExpirationBufferSec());
        assertEquals(expTotpAuthnSessionIdCookieName, AppConfig.getTotpAuthnSessionIdCookieName());
        assertEquals(expTotpAuthnSessionIssueDateCookieName, AppConfig.getTotpAuthnSessionIssueDateCookieName());
        assertEquals(expTotpAuthnSessionExpirationSec, AppConfig.getTotpAuthnSessionExpirationSec());
        assertEquals(expTotpAuthnSessionIdSalt, AppConfig.getTotpAuthnSessionIdSalt());
    }

}
