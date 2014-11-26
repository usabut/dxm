/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.services.usermanager;

import java.io.Serializable;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.RowIterator;

import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.query.QueryWrapper;

/**
 * User path by user name cache helper.
 * 
 * @author Sergiy Shyrkov
 */
public class UserCacheHelper {

    class UserPathByUserNameCacheEntryFactory implements CacheEntryFactory {
        @Override
        public Object createEntry(final Object key) throws Exception {
            UserPathCacheKey skey = (UserPathCacheKey) key;
            String path = internalGetUserPath(skey.user, skey.site);
            if (path != null) {
                return new Element(key, path);
            } else {
                return new Element(key, StringUtils.EMPTY, 0, timeToLiveForEmptyPath);
            }
        }
    }

    static final class UserPathCacheKey implements Serializable {
        private static final long serialVersionUID = -727853070149556455L;
        final int hash;
        final String site;
        final String user;

        UserPathCacheKey(String user, String site) {
            super();
            this.user = user;
            this.site = site;
            this.hash = getHashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (null == other || other.getClass() != this.getClass()) {
                return false;
            }

            UserPathCacheKey otherKey = (UserPathCacheKey) other;

            return StringUtils.equals(this.user, otherKey.user) && StringUtils.equals(this.site, otherKey.site);
        }

        private final int getHashCode() {
            int iTotal = 17;
            iTotal = 37 * iTotal + (user != null ? user.hashCode() : 0);
            iTotal = 37 * iTotal + (site != null ? site.hashCode() : 0);

            return iTotal;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private EhCacheProvider ehCacheProvider;

    private int timeToLiveForEmptyPath = 600;

    private SelfPopulatingCache userPathByUserNameCache;

    public int getTimeToLiveForEmptyPath() {
        return timeToLiveForEmptyPath;
    }

    public String getUserPath(String name, String site) {
        final String value = (String) getUserPathByUserNameCache().get(
                new UserPathCacheKey(name, StringUtils.isEmpty(site) ? StringUtils.EMPTY : "/sites/" + site))
                .getObjectValue();
        if (value.equals(StringUtils.EMPTY)) {
            return null;
        }
        return value;
    }

    private SelfPopulatingCache getUserPathByUserNameCache() {
        if (userPathByUserNameCache == null) {
            userPathByUserNameCache = ehCacheProvider.registerSelfPopulatingCache(
                    "org.jahia.services.usermanager.JahiaUserManagerService.userPathByUserNameCache",
                    new UserPathByUserNameCacheEntryFactory());
        }
        return userPathByUserNameCache;
    }

    private String internalGetUserPath(String name, String path) throws RepositoryException {
        final QueryWrapper query = JCRSessionFactory
                .getInstance()
                .getCurrentSystemSession(null, null, null)
                .getWorkspace()
                .getQueryManager()
                .createQuery(
                        "SELECT [j:nodename] from [jnt:user] where localname()='" + name + "' and isdescendantnode('"
                                + path + "/users/')", Query.JCR_SQL2);
        RowIterator it = query.execute().getRows();
        if (!it.hasNext()) {
            return null;
        }
        return it.nextRow().getPath();
    }

    public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    public void setTimeToLiveForEmptyPath(int timeToLiveForEmptyPath) {
        this.timeToLiveForEmptyPath = timeToLiveForEmptyPath;
    }

    public void updateAdded(String userPath) {
        String sitePath = userPath.startsWith("/sites/") ? "/sites/"
                + StringUtils.substringBetween(userPath, "/sites/", "/") : StringUtils.EMPTY;
        getUserPathByUserNameCache().put(
                new Element(new UserPathCacheKey(StringUtils.substringAfterLast(userPath, "/"), sitePath), userPath));
    }

    public void updateRemoved(String userPath) {
        String sitePath = userPath.startsWith("/sites/") ? "/sites/"
                + StringUtils.substringBetween(userPath, "/sites/", "/") : StringUtils.EMPTY;
        getUserPathByUserNameCache().put(
                new Element(new UserPathCacheKey(StringUtils.substringAfterLast(userPath, "/"), sitePath),
                        StringUtils.EMPTY, 0, timeToLiveForEmptyPath));
    }

}