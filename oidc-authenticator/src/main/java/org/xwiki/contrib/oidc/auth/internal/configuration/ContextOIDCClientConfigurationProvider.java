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
import javax.servlet.http.HttpServletRequest;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.container.Container;

/**
 * Provide the {@link OIDCClientConfiguration} corresponding to the current context.
 *
 * @since 1.29
 * @version $Id$
 */
@Component
@Singleton
public class ContextOIDCClientConfigurationProvider implements Provider<OIDCClientConfiguration>
{
    /**
     * The name of the cookie in which the OIDC client configuration is defined.
     */
    public static final String OIDC_CONFIGURATION_REQUEST_PROPERTY = "oidcClientConfiguration";

    @Inject
    private Container container;

    @Inject
    private ComponentManager componentManager;

    @Override
    public OIDCClientConfiguration get()
    {
        try {
            if (container.getRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) container.getRequest();

                for (Cookie cookie : request.getCookies()) {
                    if (OIDC_CONFIGURATION_REQUEST_PROPERTY.equals(cookie.getName())
                        && componentManager.hasComponent(OIDCClientConfiguration.class, cookie.getValue())) {
                        return componentManager.getInstance(OIDCClientConfiguration.class, cookie.getValue());
                    }
                }
            }

            // Return the default config if nothing is found
            return componentManager.getInstance(OIDCClientConfiguration.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load OIDC Client configuration", e);
        }
    }
}
