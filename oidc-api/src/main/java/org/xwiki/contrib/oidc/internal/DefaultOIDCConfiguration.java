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
package org.xwiki.contrib.oidc.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Default implementation of an {@link OIDCConfiguration} for providing utility methods.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Singleton
public class DefaultOIDCConfiguration implements OIDCConfiguration
{
    @Inject
    private ConfigurationSource configuration;

    @Override
    public String getGroupClaim()
    {
        return getProperty(PROP_GROUPS_CLAIM, DEFAULT_GROUPSCLAIM);
    }

    protected  <T> T getProperty(String key, T def)
    {
        // Get property from configuration
        return this.configuration.getProperty(key, def);
    }
}
