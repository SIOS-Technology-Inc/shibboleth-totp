/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2020 SIOS Technology, Inc.
 */
package com.sios.idp.shibboleth.common;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sios.idp.shibboleth.exception.ConfigInitializationException;

/**
 * TOTP認証用のサーブレットコンテキストリスナです.
 * @author SIOS Technology, Inc.
 */
public class TotpAuthnServletContextListener implements ServletContextListener {

    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // no implementation.
    }

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ConfigLoadable loader = new AppConfigLoader(event.getServletContext());
        try {
            loader.load(AppConfig.class);
        } catch (ConfigInitializationException e) {
            throw new RuntimeException("AppConfigの初期化に失敗しました。", e);
        }
    }
}
