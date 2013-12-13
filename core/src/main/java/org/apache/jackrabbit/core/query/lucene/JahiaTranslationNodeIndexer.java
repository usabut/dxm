/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.state.*;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.tika.parser.Parser;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.LuceneUtils;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author Christophe Laprun
 */
public class JahiaTranslationNodeIndexer extends JahiaNodeIndexer {
    private static final Name MIXIN_TYPES = NameFactoryImpl.getInstance().create(Name.NS_JCR_URI, "mixinTypes");
    private static final Name PRIMARY_TYPE = NameFactoryImpl.getInstance().create(Name.NS_JCR_URI, "primaryType");

    private String language;

    protected JahiaTranslationNodeIndexer(NodeState node, ItemStateManager stateProvider, NamespaceMappings mappings,
                                          Executor executor, Parser parser, QueryHandlerContext context,
                                          NodeTypeRegistry typeRegistry, NamespaceRegistry nameRegistry,
                                          ExtendedNodeType nodeType) {
        super(node, stateProvider, mappings, executor, parser, context, typeRegistry, nameRegistry, nodeType);

        try {
            for (Name propName : node.getPropertyNames()) {
                if ("language".equals(propName.getLocalName())
                        && Name.NS_JCR_URI.equals(propName.getNamespaceURI())) {
                    PropertyId id = new PropertyId(node.getNodeId(), propName);
                    PropertyState propState = (PropertyState) stateProvider.getItemState(id);
                    language = propState.getValues()[0].getString();
                    break;
                }
            }
        } catch (Exception e) {
            // shouldn't happen
            if (logger.isDebugEnabled()) {
                logger.debug("Error finding language property", e);
            }
        }
    }

    @Override
    protected ExtendedPropertyDefinition getPropertyDefinition(String fieldName) throws RepositoryException, ItemStateException {
        final int endIndex = fieldName.lastIndexOf("_" + language);
        if (endIndex >= 0) {
            fieldName = fieldName.substring(0, endIndex);
        }

        final NodeState parentNode = (NodeState) stateProvider.getItemState(node.getParentId());
        if (parentNode == null) {
            return super.getPropertyDefinition(fieldName);
        } else {
            final Name parenteNodeTypeName = parentNode.getNodeTypeName();
            final ExtendedNodeType parentNodeType = nodeTypeRegistry.getNodeType(
                    namespaceRegistry.getPrefix(parenteNodeTypeName.getNamespaceURI())
                            + ":" + parenteNodeTypeName.getLocalName()
            );
            // try to get the property definition on the parent first since we're dealing with a translation node
            ExtendedPropertyDefinition propDef = getPropertyDefinitionFor(fieldName, parentNodeType, parentNode);

            // if we haven't found the property on the parent, it might be on this node so try this
            return propDef != null ? propDef : getPropertyDefinitionFor(fieldName, nodeType, node);
        }
    }

    @Override
    protected String getFullTextFieldName(String site) {
        return LuceneUtils.getFullTextFieldName(site, language);
    }

    @Override
    public Document createDoc() throws RepositoryException {
        final Document doc = super.createDoc();

        doNotUseInExcerpt.clear();

        try {
            NodeState parentNode = (NodeState) stateProvider.getItemState(node.getParentId());

            NodeId parentId = parentNode.getParentId();
            if (parentId == null) {
                logger.warn("The node " + parentNode.getId().toString() + " is in 'free floating' state. You should run a consistency check/fix on the repository.");
            } else {
                doc.add(new Field(
                        TRANSLATED_NODE_PARENT, false, parentId.toString(),
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
            }

            if (language == null) {
                logger.warn("The node " + node.getId().toString() + " which is of type " + Constants.JAHIANT_TRANSLATION + " but doesn't contain a valid value for the jcr:language property !");
            } else {
                doc.add(new Field(TRANSLATION_LANGUAGE, language, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
            }

            // copy properties from parent into translation node, including node types
            Set<Name> parentNodePropertyNames = new HashSet<Name>(parentNode.getPropertyNames());
            final Set<Name> localNames = new HashSet<Name>(node.getPropertyNames());
            localNames.remove(PRIMARY_TYPE);
            localNames.remove(MIXIN_TYPES);
            parentNodePropertyNames.removeAll(localNames);
            parentNodePropertyNames.removeAll(getIndexingConfig().getExcludesFromI18NCopy());

            for (Name propName : parentNodePropertyNames) {
                try {
                    PropertyId id = new PropertyId(parentNode.getNodeId(), propName);
                    PropertyState propState = (PropertyState) stateProvider.getItemState(id);

                    // add each property to the _PROPERTIES_SET for searching
                    // beginning with V2
                    if (indexFormatVersion.getVersion() >= IndexFormatVersion.V2.getVersion()) {
                        addPropertyName(doc, propState.getName());
                    }

                    InternalValue[] values = propState.getValues();
                    for (InternalValue value : values) {
                        addValue(doc, value, propState.getName());
                    }
                    if (values.length > 1) {
                        // real multi-valued
                        addMVPName(doc, propState.getName());
                    }
                } catch (NoSuchItemStateException e) {
                    throwRepositoryException(e);
                } catch (ItemStateException e) {
                    throwRepositoryException(e);
                }
            }

            if (isIndexed(J_VISIBILITY) && parentNode.hasChildNodeEntry(J_VISIBILITY)) {
                doc.add(new Field(CHECK_VISIBILITY, false, "1",
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
                        Field.TermVector.NO));
            }

            // now add fields that are not used in excerpt (must go at the end)
            for (Fieldable field : doNotUseInExcerpt) {
                doc.add(field);
            }
        } catch (ItemStateException e) {
            logger.error(e.getMessage(), e);
        }

        return doc;
    }
}
