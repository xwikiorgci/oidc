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

import java.lang.reflect.Type;
import java.util.Arrays;

import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentScope;
import org.xwiki.container.Container;
import org.xwiki.contrib.oidc.provider.internal.OIDCManager;
import org.xwiki.instance.InstanceIdManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.properties.ConverterManager;

import com.xpn.xwiki.api.Object;

/**
 * Represents an OIDC client configuration stored in an XWiki Object.
 *
 * @version $Id$
 * @since 1.29
 */
public class WikiOIDCClientConfiguration extends AbstractOIDCClientConfiguration implements WikiComponent
{
    /**
     * The name of the configuration class.
     */
    public static final String CLASS_NAME = "ClientConfigurationClass";

    /**
     * The String reference of the class defining the object which contains an OIDC configuration.
     */
    public static final String CLASS_FULLNAME = "XWiki.OIDC." + CLASS_NAME;

    /**
     * The local reference of the configuration class.
     */
    public static final LocalDocumentReference CLASS_LOCAL_REFERENCE = new LocalDocumentReference(Arrays.asList(
        "XWiki", "OIDC"), CLASS_NAME);

    /**
     * The name of the configuration.
     */
    public static final String FIELD_CONFIGURATION_NAME = "configurationName";

    private DocumentReference documentReference;

    private DocumentReference authorReference;

    private Object xobject;

    private ComponentManager componentManager;

    /**
     * Builds a new {@link WikiOIDCClientConfiguration}.
     *
     * @param authorReference the reference of the author creating the component
     * @param xobject the object holding the component configuration
     * @param componentManager the component manager
     * @throws ComponentLookupException if the initialization failed
     */
    public WikiOIDCClientConfiguration(DocumentReference authorReference,
        Object xobject, ComponentManager componentManager) throws ComponentLookupException
    {
        this.documentReference = xobject.getDocumentReference();
        this.authorReference = authorReference;
        this.xobject = xobject;
        this.componentManager = componentManager;

        this.logger = componentManager.getInstance(Logger.class);
        this.container = componentManager.getInstance(Container.class);
        this.manager = componentManager.getInstance(OIDCManager.class);
        this.instance = componentManager.getInstance(InstanceIdManager.class);
        this.converter = componentManager.getInstance(ConverterManager.class);
    }

    @Override
    public DocumentReference getDocumentReference()
    {
        return documentReference;
    }

    @Override
    public DocumentReference getAuthorReference()
    {
        return authorReference;
    }

    @Override
    public Type getRoleType()
    {
        return OIDCClientConfiguration.class;
    }

    @Override
    public String getRoleHint()
    {
        return converter.convert(String.class, xobject.getValue(FIELD_CONFIGURATION_NAME));
    }

    @Override
    public WikiComponentScope getScope()
    {
        return WikiComponentScope.GLOBAL;
    }

    @Override
    protected <T> T getFallbackProperty(String key, Class<T> valueClass)
    {
        return converter.convert(valueClass, xobject.getValue(key));
    }

    @Override
    protected <T> T getFallbackProperty(String key, T def)
    {
        return converter.convert(def.getClass(), xobject.getValue(key));
    }
}
