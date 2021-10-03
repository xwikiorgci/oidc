/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.oidc.auth.internal.configuration;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.oidc.internal.OIDCConfiguration;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiServletRequest;

/**
 * Provide the {@link OIDCClientConfiguration} corresponding to the current context.
 *
 * The choice of the client configuration takes several configuration into account :
 * <ul>
 *     <li>The provider will first check if a cookie defined in CLIENT_CONFIGURATION_COOKIE_PROPERTY exists, forcing
 *     the use of a given client configuration.</li>
 *     <li>If no cookie is defined, or if a cookie is defined but no configuration exist with this name, the provider
 *     will fallback on the configuration defined in DEFAULT_CLIENT_CONFIGURATION_PROPERTY.</li>
 *     <li>If the fallback configuration does not exist, the provider will fallback on the default configuration
 *     available through the component manager.</li>
 * </ul>
 *
 * @since 1.29
 * @version $Id$
 */
@Component
@Singleton
public class ContextOIDCClientConfigurationProvider implements Provider<OIDCClientConfiguration>
{
    /**
     * The name of the property in which the name of the OIDC configuration should be stored.
     */
    public static final String CLIENT_CONFIGURATION_COOKIE_PROPERTY = OIDCConfiguration.PREFIX_PROP
        + "clientConfigurationCookie";

    /**
     * The default name of the cookie in which the OIDC client configuration is defined.
     */
    public static final String DEFAULT_OIDC_CONFIGURATION_COOKIE = "oidcProvider";

    /**
     *
     */
    public static final String DEFAULT_CLIENT_CONFIGURATION_PROPERTY = OIDCConfiguration.PREFIX_PROP
        + "defaultClientConfiguration";

    /**
     * Default client configuration to use when no configuration is defined.
     */
    public static final String DEFAULT_CLIENT_CONFIGURATION = "default";

    @Inject
    private ConfigurationSource configurationSource;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override
    public OIDCClientConfiguration get()
    {
        String cookieName = configurationSource.getProperty(CLIENT_CONFIGURATION_COOKIE_PROPERTY,
            DEFAULT_OIDC_CONFIGURATION_COOKIE);

        String fallbackProviderName = configurationSource.getProperty(DEFAULT_CLIENT_CONFIGURATION_PROPERTY,
            DEFAULT_CLIENT_CONFIGURATION);

        try {
            // Check if a cookie exists, indicating which configuration to use
            XWikiContext context = contextProvider.get();
            if (context.getRequest() instanceof XWikiServletRequest) {
                XWikiServletRequest request = (XWikiServletRequest) context.getRequest();

                Cookie cookie = request.getCookie(cookieName);
                if (cookie != null && componentManager.hasComponent(OIDCClientConfiguration.class, cookie.getValue())) {
                    return componentManager.getInstance(OIDCClientConfiguration.class, cookie.getValue());
                }
            }

            // Try to fallback on the configuration provided in the XWiki configuration
            if (componentManager.hasComponent(OIDCClientConfiguration.class, fallbackProviderName)) {
                return componentManager.getInstance(OIDCClientConfiguration.class, fallbackProviderName);
            }

            // Return the default config if nothing is found
            return componentManager.getInstance(OIDCClientConfiguration.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load OIDC Client configuration", e);
        }
    }
}
