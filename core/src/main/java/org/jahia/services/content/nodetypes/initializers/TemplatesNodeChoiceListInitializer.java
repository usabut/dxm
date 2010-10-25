/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.nodetypes.initializers;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 1, 2010
 * Time: 3:37:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TemplatesNodeChoiceListInitializer implements ChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(TemplatesNodeChoiceListInitializer.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
        if (node == null) {
            node = (JCRNodeWrapper) context.get("contextParent");
        }

        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();

        try {
            JCRNodeWrapper site = node.getResolveSite();
            final JCRSessionWrapper session = site.getSession();
            final QueryManager queryManager = session.getWorkspace().getQueryManager();
            QueryResult result = queryManager.createQuery("select * from [jnt:pageTemplate] as n where isdescendantnode(n,['"+site.getPath()+"'])", Query.JCR_SQL2).execute();
            final NodeIterator iterator = result.getNodes();
            while (iterator.hasNext()) {
                JCRNodeWrapper tpl = (JCRNodeWrapper) iterator.next();
                vs.add(new ChoiceListValue(tpl.getName(), null, session.getValueFactory().createValue(tpl.getIdentifier(),
                        PropertyType.WEAKREFERENCE)));
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get template",e);
        }


        return vs;
    }
}
