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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.xwiki.container.Container;
import org.xwiki.container.Request;
import org.xwiki.container.Session;
import org.xwiki.container.servlet.ServletSession;
import org.xwiki.contrib.oidc.auth.internal.Endpoint;
import org.xwiki.contrib.oidc.provider.internal.OIDCManager;
import org.xwiki.contrib.oidc.provider.internal.endpoint.AuthorizationOIDCEndpoint;
import org.xwiki.contrib.oidc.provider.internal.endpoint.LogoutOIDCEndpoint;
import org.xwiki.contrib.oidc.provider.internal.endpoint.TokenOIDCEndpoint;
import org.xwiki.contrib.oidc.provider.internal.endpoint.UserInfoOIDCEndpoint;
import org.xwiki.instance.InstanceIdManager;
import org.xwiki.properties.ConverterManager;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCClaimsRequest;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;

public abstract class AbstractOIDCClientConfiguration implements OIDCClientConfiguration
{
    @Inject
    protected Logger logger;

    @Inject
    protected Container container;

    @Inject
    protected OIDCManager manager;

    @Inject
    protected InstanceIdManager instance;

    @Inject
    protected ConverterManager converter;

    /**
     * @param key the name of the property
     * @param valueClass the class of the property
     * @return the property value
     */
    protected abstract <T> T getFallbackProperty(String key, Class<T> valueClass);

    /**
     * @param key the name of the property
     * @param def the default value
     * @return the property value
     */
    protected abstract <T> T getFallbackProperty(String key, T def);

    /**
     * @param key the name of the property
     * @return the property value
     */
    public Map<String, String> getMap(String key)
    {
        List<String> list = getProperty(key, List.class);

        Map<String, String> mapping;

        if (list != null && !list.isEmpty()) {
            mapping = new HashMap<>(list.size());

            for (String listItem : list) {
                int index = listItem.indexOf('=');

                if (index != -1) {
                    mapping.put(listItem.substring(0, index), listItem.substring(index + 1));
                }
            }
        } else {
            mapping = null;
        }

        return mapping;
    }

    protected <T> T getProperty(String key, Class<T> valueClass)
    {
        // Get property from request
        String requestValue = getRequestParameter(key);
        if (requestValue != null) {
            return this.converter.convert(valueClass, requestValue);
        }

        // Get property from session
        T sessionValue = getSessionAttribute(key);
        if (sessionValue != null) {
            return sessionValue;
        }

        // Get property from configuration
        return getFallbackProperty(key, valueClass);
    }

    protected <T> T getProperty(String key, T def)
    {
        // Get property from request
        String requestValue = getRequestParameter(key);
        if (requestValue != null) {
            return this.converter.convert(def.getClass(), requestValue);
        }

        // Get property from session
        T sessionValue = getSessionAttribute(key);
        if (sessionValue != null) {
            return sessionValue;
        }

        // Get property from configuration
        return getFallbackProperty(key, def);
    }

    protected String getRequestParameter(String key)
    {
        Request request = this.container.getRequest();
        if (request != null) {
            return (String) request.getProperty(key);
        }

        return null;
    }

    protected HttpSession getHttpSession()
    {
        Session session = this.container.getSession();
        if (session instanceof ServletSession) {
            HttpSession httpSession = ((ServletSession) session).getHttpSession();

            this.logger.debug("Session: {}", httpSession.getId());

            return httpSession;
        }

        return null;
    }

    protected  <T> T getSessionAttribute(String name)
    {
        HttpSession session = getHttpSession();
        if (session != null) {
            return (T) session.getAttribute(name);
        }

        return null;
    }

    protected  <T> T removeSessionAttribute(String name)
    {
        HttpSession session = getHttpSession();
        if (session != null) {
            try {
                return (T) session.getAttribute(name);
            } finally {
                session.removeAttribute(name);
            }
        }

        return null;
    }

    protected void setSessionAttribute(String name, Object value)
    {
        HttpSession session = getHttpSession();
        if (session != null) {
            session.setAttribute(name, value);
        }
    }

    @Override
    public String getGroupClaim()
    {
        return getProperty(PROP_GROUPS_CLAIM, DEFAULT_GROUPSCLAIM);
    }

    @Override
    public String getSubjectFormatter()
    {
        String userFormatter = getProperty(PROP_USER_SUBJECTFORMATER, String.class);
        if (userFormatter == null) {
            userFormatter = DEFAULT_USER_SUBJECTFORMATER;
        }

        return userFormatter;
    }

    @Override
    public String getXWikiUserNameFormatter()
    {
        String userFormatter = getProperty(PROP_USER_NAMEFORMATER, String.class);
        if (userFormatter == null) {
            userFormatter = DEFAULT_USER_NAMEFORMATER;
        }

        return userFormatter;
    }

    @Override
    public Map<String, String> getUserMapping()
    {
        return getMap(PROP_USER_MAPPING);
    }

    @Override
    public URL getXWikiProvider()
    {
        return getProperty(PROP_XWIKIPROVIDER, URL.class);
    }

    private Endpoint getEndPoint(String hint) throws URISyntaxException
    {
        // TODO: use URI directly when upgrading to a version of XWiki providing a URI converter
        String uriString = getProperty(PROPPREFIX_ENDPOINT + hint, String.class);

        // If no direct endpoint is provider assume it's a XWiki OIDC provider and generate the endpoint from the hint
        URI uri;
        if (uriString == null) {
            if (getProperty(PROP_XWIKIPROVIDER, String.class) != null) {
                uri = this.manager.createEndPointURI(getXWikiProvider().toString(), hint);
            } else {
                uri = null;
            }
        } else {
            uri = new URI(uriString);
        }

        // If we still don't have any endpoint URI, return null
        if (uri == null) {
            return null;
        }

        // Find custom headers
        Map<String, List<String>> headers = new LinkedHashMap<>();

        List<String> entries = getProperty(PROPPREFIX_ENDPOINT + hint + ".headers", List.class);
        if (entries != null) {
            for (String entry : entries) {
                int index = entry.indexOf(':');

                if (index > 0 && index < entry.length() - 1) {
                    headers.computeIfAbsent(entry.substring(0, index), key -> new ArrayList<>())
                        .add(entry.substring(index + 1));
                }
            }
        }

        return new Endpoint(uri, headers);
    }

    @Override
    public Endpoint getAuthorizationOIDCEndpoint() throws URISyntaxException
    {
        return getEndPoint(AuthorizationOIDCEndpoint.HINT);
    }

    @Override
    public Endpoint getTokenOIDCEndpoint() throws URISyntaxException
    {
        return getEndPoint(TokenOIDCEndpoint.HINT);
    }

    @Override
    public Endpoint getUserInfoOIDCEndpoint() throws URISyntaxException
    {
        return getEndPoint(UserInfoOIDCEndpoint.HINT);
    }

    @Override
    public Endpoint getLogoutOIDCEndpoint() throws URISyntaxException
    {
        return getEndPoint(LogoutOIDCEndpoint.HINT);
    }

    @Override
    public ClientID getClientID()
    {
        String clientId = getProperty(PROP_CLIENTID, String.class);

        // Fallback on instance id
        return new ClientID(clientId != null ? clientId : this.instance.getInstanceId().getInstanceId());
    }

    @Override
    public Secret getSecret()
    {
        String secret = getProperty(PROP_SECRET, String.class);
        if (StringUtils.isBlank(secret)) {
            return null;
        } else {
            return new Secret(secret);
        }
    }

    @Override
    public ClientAuthenticationMethod getTokenEndPointAuthMethod()
    {
        String authMethod = getProperty(PROP_ENDPOINT_TOKEN_AUTH_METHOD, String.class);
        if ("client_secret_post".equalsIgnoreCase(authMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_POST;
        } else {
            return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        }
    }

    @Override
    public HTTPRequest.Method getUserInfoEndPointMethod()
    {
        return getProperty(PROP_ENDPOINT_USERINFO_METHOD, HTTPRequest.Method.GET);
    }

    @Override
    public HTTPRequest.Method getLogoutEndPointMethod()
    {
        return getProperty(PROP_ENDPOINT_LOGOUT_METHOD, HTTPRequest.Method.GET);
    }

    @Override
    public String getSessionState()
    {
        return getSessionAttribute(PROP_STATE);
    }

    @Override
    public boolean isSkipped()
    {
        return getProperty(PROP_SKIPPED, false);
    }

    @Override
    public OIDCClaimsRequest getClaimsRequest()
    {
        // TODO: allow passing the complete JSON as configuration
        OIDCClaimsRequest claimsRequest = new OIDCClaimsRequest();

        // ID Token claims
        List<String> idtokenclaims = getIDTokenClaims();
        if (idtokenclaims != null && !idtokenclaims.isEmpty()) {
            ClaimsSetRequest idtokenclaimsRequest = new ClaimsSetRequest();

            for (String claim : idtokenclaims) {
                idtokenclaimsRequest.add(claim);
            }

            claimsRequest.withIDTokenClaimsRequest(idtokenclaimsRequest);
        }

        // UserInfo claims
        List<String> userinfoclaims = getUserInfoClaims();
        if (userinfoclaims != null && !userinfoclaims.isEmpty()) {
            ClaimsSetRequest userinfoclaimsRequest = new ClaimsSetRequest();

            for (String claim : userinfoclaims) {
                userinfoclaimsRequest.add(claim);
            }

            claimsRequest.withUserInfoClaimsRequest(userinfoclaimsRequest);
        }

        return claimsRequest;
    }

    @Override
    public List<String> getIDTokenClaims()
    {
        return getProperty(PROP_IDTOKENCLAIMS, DEFAULT_IDTOKENCLAIMS);
    }

    @Override
    public List<String> getUserInfoClaims()
    {
        return getProperty(PROP_USERINFOCLAIMS, DEFAULT_USERINFOCLAIMS);
    }

    @Override
    public int getUserInfoRefreshRate()
    {
        return getProperty(PROP_USERINFOREFRESHRATE, 600000);
    }

    @Override
    public Scope getAuthorizationScope()
    {
        List<String> scopeValues = getProperty(PROP_SCOPE, List.class);

        if (CollectionUtils.isEmpty(scopeValues)) {
            return new Scope(OIDCScopeValue.OPENID, OIDCScopeValue.PROFILE, OIDCScopeValue.EMAIL,
                OIDCScopeValue.ADDRESS, OIDCScopeValue.PHONE);
        }

        return new Scope(scopeValues.toArray(new String[0]));
    }

    @Override
    public GroupMapping getGroupMapping()
    {
        List<String> groupsMapping = getProperty(PROP_GROUPS_MAPPING, List.class);

        GroupMapping groups;

        if (groupsMapping != null && !groupsMapping.isEmpty()) {
            groups = new GroupMapping(groupsMapping.size());

            for (String groupMapping : groupsMapping) {
                int index = groupMapping.indexOf('=');

                if (index != -1) {
                    String xwikiGroup = toXWikiGroup(groupMapping.substring(0, index));
                    String providerGroup = groupMapping.substring(index + 1);

                    // Add to XWiki mapping
                    Set<String>
                        providerGroups = groups.getXWikiMapping().computeIfAbsent(xwikiGroup, k -> new HashSet<>());
                    providerGroups.add(providerGroup);

                    // Add to provider mapping
                    Set<String> xwikiGroups =
                        groups.getProviderMapping().computeIfAbsent(providerGroup, k -> new HashSet<>());
                    xwikiGroups.add(xwikiGroup);
                }
            }
        } else {
            groups = null;
        }

        return groups;
    }

    @Override
    public List<String> getAllowedGroups()
    {
        List<String> groups = getProperty(PROP_GROUPS_ALLOWED, List.class);

        return groups != null && !groups.isEmpty() ? groups : null;
    }

    @Override
    public List<String> getForbiddenGroups()
    {
        List<String> groups = getProperty(PROP_GROUPS_FORBIDDEN, List.class);

        return groups != null && !groups.isEmpty() ? groups : null;
    }

    @Override
    public String getGroupPrefix()
    {
        String groupPrefix = getProperty(PROP_GROUPS_PREFIX, String.class);
        return groupPrefix != null && !groupPrefix.isEmpty() ? groupPrefix : null;
    }

    @Override
    public String getGroupSeparator()
    {
        return getProperty(PROP_GROUPS_SEPARATOR, String.class);
    }

    // Session only

    @Override
    public Date removeUserInfoExpirationDate()
    {
        return removeSessionAttribute(PROP_SESSION_USERINFO_EXPORATIONDATE);
    }

    @Override
    public void setUserInfoExpirationDate(Date date)
    {
        setSessionAttribute(PROP_SESSION_USERINFO_EXPORATIONDATE, date);
    }

    @Override
    public void resetUserInfoExpirationDate()
    {
        LocalDateTime expiration = LocalDateTime.now().plusMillis(getUserInfoRefreshRate());

        setUserInfoExpirationDate(expiration.toDate());
    }

    @Override
    public BearerAccessToken getAccessToken()
    {
        return getSessionAttribute(PROP_SESSION_ACCESSTOKEN);
    }

    @Override
    public void setAccessToken(BearerAccessToken accessToken)
    {
        setSessionAttribute(PROP_SESSION_ACCESSTOKEN, accessToken);
    }

    @Override
    public IDTokenClaimsSet getIdToken()
    {
        return getSessionAttribute(PROP_SESSION_IDTOKEN);
    }

    @Override
    public void setIdToken(IDTokenClaimsSet idToken)
    {
        setSessionAttribute(PROP_SESSION_IDTOKEN, idToken);
    }

    @Override
    public URI getSuccessRedirectURI()
    {
        URI uri = getSessionAttribute(PROP_INITIAL_REQUEST);
        if (uri == null) {
            // TODO: return wiki home page
        }

        return uri;
    }

    @Override
    public void setSuccessRedirectURI(URI uri)
    {
        setSessionAttribute(PROP_INITIAL_REQUEST, uri);
    }

    @Override
    public boolean isGroupSync()
    {
        String groupClaim = getGroupClaim();

        return getUserInfoClaims().contains(groupClaim);
    }
}
