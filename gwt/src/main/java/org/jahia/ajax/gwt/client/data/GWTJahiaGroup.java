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

package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 5, 2008
 * Time: 2:53:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaGroup extends BaseModelData implements GWTJahiaPrincipal {
    public GWTJahiaGroup() {
    }

    public GWTJahiaGroup(String groupName, String groupKey) {
        setGroupname(groupName);
        setGroupKey(groupKey) ;
    }

    public String getGroupname() {
        return get("groupname") ;
    }

    public String getName() {
        return get("name") ;
    }

    public void setGroupname(String username) {
        set("groupname", username) ;
        set("name", username) ;
    }

    public String getGroupKey() {
        return get("groupKey") ;
    }

    public String getKey() {
        return get("groupKey") ;
    }

    public void setGroupKey(String userKey) {
        set("groupKey", userKey) ;
        set("key", userKey) ;
    }

    public void setSiteName (String serverName) {
        set("siteName",serverName);
    }

    public String getSiteName(){
        return get("siteName");
    }

    public void setSiteId (Integer siteId) {
        set("siteId",siteId);
    }

    public Integer getSiteId(){
        return get("siteId");
    }

    public void setProvider (String provider) {
        set("provider",provider);
    }

    public String getProvider(){
        return get("provider");
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GWTJahiaGroup && getGroupKey().equals(((GWTJahiaGroup)obj).getGroupKey());
    }
}
