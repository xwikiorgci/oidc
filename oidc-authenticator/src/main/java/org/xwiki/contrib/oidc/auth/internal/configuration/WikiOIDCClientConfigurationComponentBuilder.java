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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.component.wiki.WikiObjectComponentBuilder;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ObjectReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Builder for {@link WikiOIDCClientConfiguration}.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Singleton
public class WikiOIDCClientConfigurationComponentBuilder implements WikiObjectComponentBuilder
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Override
    public EntityReference getClassReference()
    {
        return WikiOIDCClientConfiguration.CLASS_LOCAL_REFERENCE;
    }

    @Override
    public List<WikiComponent> buildComponents(ObjectReference reference) throws WikiComponentException
    {
        XWikiContext context = xWikiContextProvider.get();

        try {
            XWikiDocument document = context.getWiki().getDocument(reference.getDocumentReference(), context);

            BaseObject object = document.getXObject(reference);

            WikiComponent oidcClientConfiguration = new WikiOIDCClientConfiguration(document.getAuthorReference(),
                new Object(object, context), componentManager);

            return Collections.singletonList(oidcClientConfiguration);
        } catch (XWikiException | ComponentLookupException e) {
            throw new WikiComponentException(
                String.format("Failed to initialize OIDC Client configuration in [%s]", reference), e);
        }
    }
}
