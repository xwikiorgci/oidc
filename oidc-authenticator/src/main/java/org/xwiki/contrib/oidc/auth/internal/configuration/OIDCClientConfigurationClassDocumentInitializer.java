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

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.oidc.internal.OIDCConfiguration;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Document initializer for the OIDC client configuration class.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Named(WikiOIDCClientConfiguration.CLASS_FULLNAME)
@Singleton
public class OIDCClientConfigurationClassDocumentInitializer extends AbstractMandatoryClassInitializer
{
    /**
     * Builds a new {@link OIDCClientConfigurationClassDocumentInitializer}.
     */
    public OIDCClientConfigurationClassDocumentInitializer()
    {
        super(WikiOIDCClientConfiguration.CLASS_LOCAL_REFERENCE, "OpenID Connect Client Configuration Class");
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        xclass.addTextField(WikiOIDCClientConfiguration.FIELD_CONFIGURATION_NAME, "Configuration name", 255);
        xclass.addTextField(OIDCConfiguration.PROP_GROUPS_CLAIM, "Group claim", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_USER_SUBJECTFORMATER,
            "Subject formatter", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_USER_NAMEFORMATER,
            "XWiki username formatter", 255);
        xclass.addStaticListField(OIDCClientConfiguration.PROP_USER_MAPPING, "User mapping", 5, true, false,
            StringUtils.EMPTY, "input", "|,", StringUtils.EMPTY, "allowed", true);
        xclass.addTextField(OIDCClientConfiguration.PROP_XWIKIPROVIDER, "XWiki provider", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_ENDPOINT_AUTHORIZATION,
            "Authorization OIDC endpoint", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_ENDPOINT_TOKEN,
            "Token OIDC endpoint", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_ENDPOINT_USERINFO,
            "User info OIDC endpoint", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_ENDPOINT_LOGOUT,
            "Logout OIDC endpoint", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_CLIENTID, "Client ID", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_SECRET, "Secret", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_ENDPOINT_TOKEN_AUTH_METHOD,
            "Token endpoint authentication method", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_ENDPOINT_USERINFO_METHOD,
            "User information endpoint method", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_ENDPOINT_LOGOUT_METHOD,
            "Logout endpoint method", 255);
        xclass.addBooleanField(OIDCClientConfiguration.PROP_SKIPPED, "Is authentication skipped ?");
        xclass.addTextField(OIDCClientConfiguration.PROP_SCOPE, "Scope", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_IDTOKENCLAIMS, "ID Token Claims", 255);
        xclass.addTextField(OIDCClientConfiguration.PROP_USERINFOCLAIMS, "User info Claims", 255);

    }
}
