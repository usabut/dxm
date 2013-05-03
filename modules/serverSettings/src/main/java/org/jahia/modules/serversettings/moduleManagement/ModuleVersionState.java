/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.serversettings.moduleManagement;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * State information for a module version, including possible actions.
 * 
 * @author Sergiy Shyrkov
 */
public class ModuleVersionState implements Serializable {

    private static final long serialVersionUID = 7311222686981884149L;

    private boolean canBeStarted;

    private boolean canBeStopped;

    private boolean canBeUninstalled;

    private Set<String> dependencies = new TreeSet<String>();

    private boolean systemDependency;

    private Set<String> unresolvedDependencies = new TreeSet<String>();

    private Set<String> usedInSites = new TreeSet<String>();

    public Set<String> getDependencies() {
        return dependencies;
    }

    public Set<String> getUnresolvedDependencies() {
        return unresolvedDependencies;
    }

    public Set<String> getUsedInSites() {
        return usedInSites;
    }

    public boolean isCanBeStarted() {
        return canBeStarted;
    }

    public boolean isCanBeStopped() {
        return canBeStopped;
    }

    public boolean isCanBeUninstalled() {
        return canBeUninstalled;
    }

    public boolean isSystemDependency() {
        return systemDependency;
    }

    public void setCanBeStarted(boolean canBeStarted) {
        this.canBeStarted = canBeStarted;
    }

    public void setCanBeStopped(boolean canBeStopped) {
        this.canBeStopped = canBeStopped;
    }

    public void setCanBeUninstalled(boolean canBeUninstalled) {
        this.canBeUninstalled = canBeUninstalled;
    }

    public void setSystemDependency(boolean systemDependency) {
        this.systemDependency = systemDependency;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}