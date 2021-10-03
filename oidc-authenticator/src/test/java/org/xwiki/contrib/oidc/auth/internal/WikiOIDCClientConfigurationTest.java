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
package org.xwiki.contrib.oidc.auth.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.container.Container;
import org.xwiki.contrib.oidc.auth.internal.configuration.DefaultOIDCClientConfiguration;
import org.xwiki.contrib.oidc.auth.internal.configuration.OIDCClientConfiguration;
import org.xwiki.contrib.oidc.auth.internal.configuration.WikiOIDCClientConfiguration;
import org.xwiki.contrib.oidc.provider.internal.OIDCManager;
import org.xwiki.instance.InstanceIdManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Validate {@link WikiOIDCClientConfiguration}.
 * 
 * @version $Id$
 */
@ComponentTest
class WikiOIDCClientConfigurationTest
{
    @MockComponent
    Logger logger;

    @MockComponent
    Container container;

    @MockComponent
    OIDCManager oidcManager;

    @MockComponent
    InstanceIdManager instanceIdManager;

    @MockComponent
    ConverterManager converterManager;

    @InjectComponentManager
    ComponentManager componentManager;

    private WikiOIDCClientConfiguration wikiOIDCClientConfiguration;

    private DocumentReference authorReference;

    private Object xobject;

    @BeforeEach
    public void beforeEach() throws Exception
    {
        authorReference = new DocumentReference("xwiki", "XWiki", "Admin");

        BaseObject baseObject = new BaseObject();
        baseObject.setXClassReference(new DocumentReference("xwiki", Arrays.asList("XWiki", "OIDC"),
        "ClientConfiguration"));

        XWikiContext xWikiContext = new XWikiContext();

        xobject = new Object(baseObject, xWikiContext);

        wikiOIDCClientConfiguration = new WikiOIDCClientConfiguration(authorReference, xobject, logger,
            componentManager);
    }

    @Test
    void getUserInfoOIDCEndpoint() throws URISyntaxException
    {
        assertNull(wikiOIDCClientConfiguration.getUserInfoOIDCEndpoint());

        URI uri = new URI("/endpoint");
        when(xobject.getValue(OIDCClientConfiguration.PROP_ENDPOINT_USERINFO))
            .thenReturn(uri.toString());

        Endpoint endpoint = wikiOIDCClientConfiguration.getUserInfoOIDCEndpoint();

        assertEquals(uri, endpoint.getURI());
        assertTrue(endpoint.getHeaders().isEmpty());

        List<String> list = Arrays.asList("key1:value11", "key1:value12", "key2:value2", "alone", ":", "");

        when(xobject.getValue(OIDCClientConfiguration.PROP_ENDPOINT_USERINFO_HEADERS))
            .thenReturn(list);

        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("key1", Arrays.asList("value11", "value12"));
        headers.put("key2", Arrays.asList("value2"));

        endpoint = wikiOIDCClientConfiguration.getUserInfoOIDCEndpoint();

        assertEquals(uri, endpoint.getURI());
        assertEquals(headers, endpoint.getHeaders());

    }
}
