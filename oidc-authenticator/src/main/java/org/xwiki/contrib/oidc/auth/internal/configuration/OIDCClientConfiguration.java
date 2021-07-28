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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.oidc.OIDCIdToken;
import org.xwiki.contrib.oidc.OIDCUserInfo;
import org.xwiki.contrib.oidc.auth.internal.Endpoint;
import org.xwiki.contrib.oidc.internal.OIDCConfiguration;
import org.xwiki.contrib.oidc.provider.internal.endpoint.AuthorizationOIDCEndpoint;
import org.xwiki.contrib.oidc.provider.internal.endpoint.LogoutOIDCEndpoint;
import org.xwiki.contrib.oidc.provider.internal.endpoint.TokenOIDCEndpoint;
import org.xwiki.contrib.oidc.provider.internal.endpoint.UserInfoOIDCEndpoint;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCClaimsRequest;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;

/**
 * Various OpenID Connect authenticator configurations.
 * 
 * @version $Id$
 */
@Role
public interface OIDCClientConfiguration extends OIDCConfiguration
{
    String PROP_XWIKIPROVIDER = PREFIX_PROP + "xwikiprovider";

    String PROP_USER_NAMEFORMATER = PREFIX_PROP + "user.nameFormater";

    /**
     * @since 1.11
     */
    String PROP_USER_SUBJECTFORMATER = PREFIX_PROP + "user.subjectFormater";

    /**
     * @since 1.18
     */
    String PROP_USER_MAPPING = PREFIX_PROP + "user.mapping";

    String PROPPREFIX_ENDPOINT = PREFIX_PROP + "endpoint.";

    String PROP_ENDPOINT_AUTHORIZATION = PROPPREFIX_ENDPOINT + AuthorizationOIDCEndpoint.HINT;

    String PROP_ENDPOINT_TOKEN = PROPPREFIX_ENDPOINT + TokenOIDCEndpoint.HINT;

    String PROP_ENDPOINT_USERINFO = PROPPREFIX_ENDPOINT + UserInfoOIDCEndpoint.HINT;

    /**
     * @since 1.21
     */
    String PROP_ENDPOINT_LOGOUT = PROPPREFIX_ENDPOINT + LogoutOIDCEndpoint.HINT;

    String PROP_CLIENTID = PREFIX_PROP + "clientid";

    /**
     * @since 1.13
     */
    String PROP_SECRET = PREFIX_PROP + "secret";

    String PROP_SKIPPED = PREFIX_PROP + "skipped";

    /**
     * @since 1.13
     */
    String PROP_ENDPOINT_TOKEN_AUTH_METHOD =
        PROPPREFIX_ENDPOINT + TokenOIDCEndpoint.HINT + ".auth_method";

    /**
     * @since 1.13
     */
    String PROP_ENDPOINT_USERINFO_METHOD =
        PROPPREFIX_ENDPOINT + UserInfoOIDCEndpoint.HINT + ".method";

    /**
     * @since 1.22
     */
    String PROP_ENDPOINT_USERINFO_HEADERS =
        PROPPREFIX_ENDPOINT + UserInfoOIDCEndpoint.HINT + ".headers";

    /**
     * @since 1.21
     */
    String PROP_ENDPOINT_LOGOUT_METHOD = PROPPREFIX_ENDPOINT + LogoutOIDCEndpoint.HINT + ".method";

    /**
     * @since 1.12
     */
    String PROP_USERINFOREFRESHRATE = PREFIX_PROP + "userinforefreshrate";

    /**
     * @since 1.16
     */
    String PROP_SCOPE = PREFIX_PROP + "scope";

    /**
     * @since 1.10
     */
    String PROP_GROUPS_MAPPING = PREFIX_PROP + "groups.mapping";

    /**
     * @since 1.10
     */
    String PROP_GROUPS_ALLOWED = PREFIX_PROP + "groups.allowed";

    /**
     * @since 1.10
     */
    String PROP_GROUPS_FORBIDDEN = PREFIX_PROP + "groups.forbidden";

    /**
     * @since 1.27
     */
    String PROP_GROUPS_PREFIX = PREFIX_PROP + "groups.prefix";

    /**
     * @since 1.27
     */
    String PROP_GROUPS_SEPARATOR = PREFIX_PROP + "groups.separator";

    String PROP_STATE = PREFIX_PROP + "state";

    String PROP_USERINFOCLAIMS = PREFIX_PROP + "userinfoclaims";

    String PROP_IDTOKENCLAIMS = PREFIX_PROP + "idtokenclaims";

    // Default values

    String DEFAULT_USER_NAMEFORMATER = "${oidc.issuer.host._clean}-${oidc.user.preferredUsername._clean}";

    /**
     * @since 1.11
     */
    String DEFAULT_USER_SUBJECTFORMATER = "${oidc.user.subject}";

    List<String> DEFAULT_USERINFOCLAIMS = Arrays.asList(OIDCUserInfo.CLAIM_XWIKI_ACCESSIBILITY,
        OIDCUserInfo.CLAIM_XWIKI_COMPANY, OIDCUserInfo.CLAIM_XWIKI_DISPLAYHIDDENDOCUMENTS,
        OIDCUserInfo.CLAIM_XWIKI_EDITOR, OIDCUserInfo.CLAIM_XWIKI_USERTYPE);

    List<String> DEFAULT_IDTOKENCLAIMS = Arrays.asList(OIDCIdToken.CLAIM_XWIKI_INSTANCE_ID);

    // Session properties that are not used in configuration files
    String PROP_INITIAL_REQUEST = "xwiki.initialRequest";

    String PROP_SESSION_ACCESSTOKEN = PREFIX_PROP + "accesstoken";

    String PROP_SESSION_IDTOKEN = PREFIX_PROP + "idtoken";

    String PROP_SESSION_USERINFO_EXPORATIONDATE = PREFIX_PROP + "session.userinfoexpirationdate";

    String XWIKI_GROUP_PREFIX = "XWiki.";

    class GroupMapping
    {
        private final Map<String, Set<String>> xwikiMapping;

        private final Map<String, Set<String>> providerMapping;

        public GroupMapping(int size)
        {
            this.xwikiMapping = new HashMap<>(size);
            this.providerMapping = new HashMap<>(size);
        }

        public Set<String> fromXWiki(String xwikiGroup)
        {
            return this.xwikiMapping.get(xwikiGroup);
        }

        public Set<String> fromProvider(String providerGroup)
        {
            return this.providerMapping.get(providerGroup);
        }

        public Map<String, Set<String>> getXWikiMapping()
        {
            return this.xwikiMapping;
        }

        public Map<String, Set<String>> getProviderMapping()
        {
            return this.providerMapping;
        }
    }

    /**
     * @since 1.18
     */
    String getSubjectFormatter();

    /**
     * @since 1.11
     */
    String getXWikiUserNameFormatter();

    /**
     * @since 1.18
     */
    Map<String, String> getUserMapping();

    URL getXWikiProvider();

    Endpoint getAuthorizationOIDCEndpoint() throws URISyntaxException;

    Endpoint getTokenOIDCEndpoint() throws URISyntaxException;

    Endpoint getUserInfoOIDCEndpoint() throws URISyntaxException;

    /**
     * @since 1.21
     */
    Endpoint getLogoutOIDCEndpoint() throws URISyntaxException;

    ClientID getClientID();

    /**
     * @since 1.13
     */
    Secret getSecret();

    /**
     * @since 1.13
     */
    ClientAuthenticationMethod getTokenEndPointAuthMethod();

    /**
     * @since 1.13
     */
    HTTPRequest.Method getUserInfoEndPointMethod();

    /**
     * @since 1.21
     */
    HTTPRequest.Method getLogoutEndPointMethod();

    String getSessionState();

    /**
     * @return true if the authentication should be skipped
     */
    boolean isSkipped();

    /**
     * @since 1.2
     */
    OIDCClaimsRequest getClaimsRequest();

    /**
     * @since 1.2
     */
    List<String> getIDTokenClaims();

    /**
     * @since 1.2
     */
    List<String> getUserInfoClaims();

    /**
     * @since 1.12
     */
    int getUserInfoRefreshRate();

    /**
     * @since 1.2
     */
    Scope getAuthorizationScope();

    /**
     * @since 1.10
     */
    GroupMapping getGroupMapping();

    /**
     * @since 1.10
     */
    List<String> getAllowedGroups();

    /**
     * @since 1.10
     */
    List<String> getForbiddenGroups();

    /**
     * @since 1.27
     */
    String getGroupPrefix();

    /**
     * @since 1.27
     */
    String getGroupSeparator();

    // Session only

    /**
     * @since 1.2
     */
    Date removeUserInfoExpirationDate();

    /**
     * @since 1.2
     */
    void setUserInfoExpirationDate(Date date);

    /**
     * @since 1.2
     */
    void resetUserInfoExpirationDate();

    /**
     * @since 1.2
     */
    BearerAccessToken getAccessToken();

    /**
     * @since 1.2
     */
    void setAccessToken(BearerAccessToken accessToken);

    /**
     * @since 1.2
     */
    IDTokenClaimsSet getIdToken();

    /**
     * @since 1.2
     */
    void setIdToken(IDTokenClaimsSet idToken);

    /**
     * @since 1.2
     */
    URI getSuccessRedirectURI();

    /**
     * @since 1.2
     */
    void setSuccessRedirectURI(URI uri);

    /**
     * @return true if groups should be synchronized (in which case if the provider does not answer to the group claim
     *         it means the user does not belong to any group)
     * @since 1.14
     */
    boolean isGroupSync();

    /**
     * @since 1.10
     */
    default String toXWikiGroup(String group)
    {
        return group.startsWith(XWIKI_GROUP_PREFIX) ? group : XWIKI_GROUP_PREFIX + group;
    }
}
