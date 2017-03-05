/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.visibility;

import org.jahia.security.license.LicenseChangedListener;
import org.jahia.services.Conditional;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Service implementation for evaluating visibility conditions on a content item.
 *
 * @author rincevent
 * @since JAHIA 6.6
 * Created : 8/29/11
 */
public class VisibilityService implements LicenseChangedListener {
    
    public static final String NODE_NAME = "j:conditionalVisibility";

    private transient static Logger logger = LoggerFactory.getLogger(VisibilityService.class);

    private static volatile VisibilityService instance;

    private Map<String, VisibilityConditionRule> conditions = new HashMap<String, VisibilityConditionRule>(1);

    private Map<String, VisibilityConditionRule> disabledConditions = new HashMap<String, VisibilityConditionRule>(1);

    public static VisibilityService getInstance() {
        if (instance == null) {
            synchronized (VisibilityService.class) {
                if (instance == null) {
                    instance = new VisibilityService();
                }
            }
        }
        return instance;
    }

    public Map<String, VisibilityConditionRule> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, VisibilityConditionRule> conditions) {
        if (conditions != null) {
            for (Map.Entry<String, VisibilityConditionRule> cond : conditions.entrySet()) {
                addCondition(cond.getKey(), cond.getValue());
            }
        }
    }

    public void addCondition(String conditionType, VisibilityConditionRule instance) {
        if (instance instanceof Conditional) {
            if (!((Conditional) instance).evaluate()) {
                logger.info("Visibility condition of type {} is considered disabled. Skipping it.",
                        conditionType);
                this.disabledConditions.put(conditionType, instance);
                return;
            }
        }
        this.conditions.put(conditionType, instance);
    }

    public void removeCondition(String conditionType) {
        this.conditions.remove(conditionType);
        this.disabledConditions.remove(conditionType);
    }

    public boolean matchesConditions(JCRNodeWrapper node) {
        if (conditions.isEmpty()) {
            return true;
        }
        try {
            if (node.hasNode(NODE_NAME)) {
                node = node.getNode(NODE_NAME);
                boolean forceMatchAllConditions = node.getProperty("j:forceMatchAllConditions").getBoolean();
                List<JCRNodeWrapper> childrenOfType = JCRContentUtils.getChildrenOfType(node, "jnt:condition");
                if(childrenOfType.isEmpty()) {
                    return true;
                }
                if (forceMatchAllConditions) {
                    boolean matches = true;
                    for (JCRNodeWrapper nodeWrapper : childrenOfType) {
                        if (matches) {
                            VisibilityConditionRule rule = conditions.get(nodeWrapper.getPrimaryNodeTypeName());
                            if (rule != null) {
                                matches = rule.matches(nodeWrapper);
                            }
                        } else {
                            break;
                        }
                    }
                    return matches;
                } else {
                    for (JCRNodeWrapper nodeWrapper : childrenOfType) {
                        VisibilityConditionRule rule = conditions.get(nodeWrapper.getPrimaryNodeTypeName());
                        if (rule != null) {
                            if (rule.matches(nodeWrapper)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            } else {
                return true;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public Map<JCRNodeWrapper,Boolean> getConditionMatchesDetails(JCRNodeWrapper node) {
        Map<JCRNodeWrapper, Boolean> conditions = new HashMap<JCRNodeWrapper, Boolean>();
        try {
            if (node.hasNode(NODE_NAME)) {
                node = node.getNode(NODE_NAME);
                List<JCRNodeWrapper> childrenOfType = JCRContentUtils.getChildrenOfType(node, "jnt:condition");
                for (JCRNodeWrapper nodeWrapper : childrenOfType) {
                    VisibilityConditionRule rule = this.conditions.get(nodeWrapper.getPrimaryNodeTypeName());
                    if (rule != null) {
                        conditions.put(nodeWrapper,rule.matches(nodeWrapper));
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return conditions;
    }

    @Override
    public void onLicenseChanged() {
        // react on license change and re-register conditions
        for (Iterator<Map.Entry<String, VisibilityConditionRule>> it = conditions.entrySet().iterator(); it
                .hasNext();) {
            Entry<String, VisibilityConditionRule> cond = it.next();
            if (cond.getValue() instanceof Conditional) {
                if (!((Conditional) cond.getValue()).evaluate()) {
                    logger.info("Visibility condition of type {} is now disabled. Removing it.", cond.getKey());
                    it.remove();
                    disabledConditions.put(cond.getKey(), cond.getValue());
                }
            }
        }

        for (Iterator<Map.Entry<String, VisibilityConditionRule>> it = disabledConditions.entrySet().iterator(); it
                .hasNext();) {
            Entry<String, VisibilityConditionRule> cond = it.next();
            if (cond.getValue() instanceof Conditional) {
                if (((Conditional) cond.getValue()).evaluate()) {
                    logger.info("Visibility condition of type {} is now enabled. Adding it.", cond.getKey());
                    it.remove();
                    conditions.put(cond.getKey(), cond.getValue());
                }
            }
        }
    }
}
