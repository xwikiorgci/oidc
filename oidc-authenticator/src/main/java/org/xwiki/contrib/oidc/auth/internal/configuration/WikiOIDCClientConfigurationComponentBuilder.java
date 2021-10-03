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
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.component.wiki.WikiObjectComponentBuilder;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;

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
@Named(WikiOIDCClientConfiguration.CLASS_FULLNAME)
public class WikiOIDCClientConfigurationComponentBuilder implements WikiObjectComponentBuilder
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Inject
    private AuthorizationManager authorizationManager;

    @Inject
    private Logger logger;

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

            if (authorizationManager.hasAccess(Right.PROGRAM, document.getAuthorReference(),
                document.getDocumentReference())) {
                BaseObject object = document.getXObject(reference);

                WikiComponent oidcClientConfiguration =
                    new WikiOIDCClientConfiguration(document.getAuthorReference(),
                        new Object(object, context), logger, componentManager);

                return Collections.singletonList(oidcClientConfiguration);
            } else {
                return Collections.EMPTY_LIST;
            }
        } catch (XWikiException | ComponentLookupException e) {
            throw new WikiComponentException(
                String.format("Failed to initialize OIDC Client configuration in [%s]", reference), e);
        }
    }
}
