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

package org.jahia.services.cache.ehcache;

import net.sf.ehcache.statistics.StatisticsGateway;

/**
 * A bean wrapper around the new EHCache 2.7 statistics gateway to make it easy to use in JSPs or other
 * classes
 */
public class EhCacheStatisticsWrapper {

    private StatisticsGateway statisticsGateway;

    public EhCacheStatisticsWrapper(StatisticsGateway statisticsGateway) {
        this.statisticsGateway = statisticsGateway;
    }

    public long getLocalHeapSizeInBytes() {
        return statisticsGateway.getLocalHeapSizeInBytes();
    }

    public long getLocalHeapSize() {
        return statisticsGateway.getLocalHeapSize();
    }

    public long getWriterQueueLength() {
        return statisticsGateway.getWriterQueueLength();
    }

    public long getLocalDiskSize() {
        return statisticsGateway.getLocalDiskSize();
    }

    public long getLocalOffHeapSize() {
        return statisticsGateway.getLocalOffHeapSize();
    }

    public long getLocalDiskSizeInBytes() {
        return statisticsGateway.getLocalDiskSizeInBytes();
    }

    public long getLocalOffHeapSizeInBytes() {
        return statisticsGateway.getLocalOffHeapSizeInBytes();
    }

    public long getRemoteSize() {
        return statisticsGateway.getRemoteSize();
    }

    public long getSize() {
        return statisticsGateway.getSize();
    }


    public long getCacheHitCount() {
        return statisticsGateway.cacheHitCount();
    }

    public long getCacheMissExpiredCount() {
        return statisticsGateway.cacheMissExpiredCount();
    }

    public long getCacheMissNotFoundCount() {
        return statisticsGateway.cacheMissNotFoundCount();
    }

    public long getCacheMissCount() {
        return statisticsGateway.cacheMissCount();
    }

    public long getCachePutAddedCount() {
        return statisticsGateway.cachePutAddedCount();
    }

    public long getCachePutUpdatedCount() {
        return statisticsGateway.cachePutUpdatedCount();
    }

    public long getCachePutCount() {
        return statisticsGateway.cachePutCount();
    }

    public long getCacheRemoveCount() {
        return statisticsGateway.cacheRemoveCount();
    }

    public long getLocalHeapHitCount() {
        return statisticsGateway.localHeapHitCount();
    }

    public long getLocalHeapMissCount() {
        return statisticsGateway.localHeapMissCount();
    }

    public long getLocalHeapPutAddedCount() {
        return statisticsGateway.localHeapPutAddedCount();
    }

    public long getLocalHeapPutUpdatedCount() {
        return statisticsGateway.localHeapPutUpdatedCount();
    }

    public long getLocalHeapPutCount() {
        return statisticsGateway.localHeapPutCount();
    }

    public long getLocalHeapRemoveCount() {
        return statisticsGateway.localHeapRemoveCount();
    }

    public long getLocalOffHeapHitCount() {
        return statisticsGateway.localOffHeapHitCount();
    }

    public long getLocalOffHeapMissCount() {
        return statisticsGateway.localOffHeapMissCount();
    }

    public long getLocalOffHeapPutAddedCount() {
        return statisticsGateway.localOffHeapPutAddedCount();
    }

    public long getLocalOffHeapPutUpdatedCount() {
        return statisticsGateway.localOffHeapPutUpdatedCount();
    }

    public long getLocalOffHeapPutCount() {
        return statisticsGateway.localOffHeapPutCount();
    }

    public long getLocalOffHeapRemoveCount() {
        return statisticsGateway.localOffHeapRemoveCount();
    }

    public long getLocalDiskHitCount() {
        return statisticsGateway.localDiskHitCount();
    }

    public long getLocalDiskMissCount() {
        return statisticsGateway.localDiskMissCount();
    }

    public long getLocalDiskPutAddedCount() {
        return statisticsGateway.localDiskPutAddedCount();
    }

    public long getLocalDiskPutUpdatedCount() {
        return statisticsGateway.localDiskPutUpdatedCount();
    }

    public long getLocalDiskPutCount() {
        return statisticsGateway.localDiskPutCount();
    }

    public long getLocalDiskRemoveCount() {
        return statisticsGateway.localDiskRemoveCount();
    }

    public long getXaCommitReadOnlyCount() {
        return statisticsGateway.xaCommitReadOnlyCount();
    }

    public long getXaCommitExceptionCount() {
        return statisticsGateway.xaCommitExceptionCount();
    }

    public long getXaCommitCommittedCount() {
        return statisticsGateway.xaCommitCommittedCount();
    }

    public long getXaCommitCount() {
        return statisticsGateway.xaCommitCount();
    }

    public long getXaRecoveryNothingCount() {
        return statisticsGateway.xaRecoveryNothingCount();
    }

    public long getXaRecoveryRecoveredCount() {
        return statisticsGateway.xaRecoveryRecoveredCount();
    }

    public long getXaRecoveryCount() {
        return statisticsGateway.xaRecoveryCount();
    }

    public long getXaRollbackExceptionCount() {
        return statisticsGateway.xaRollbackExceptionCount();
    }

    public long getXaRollbackSuccessCount() {
        return statisticsGateway.xaRollbackSuccessCount();
    }

    public long getXaRollbackCount() {
        return statisticsGateway.xaRollbackCount();
    }

    public long getCacheExpiredCount() {
        return statisticsGateway.cacheExpiredCount();
    }

    public long getCacheEvictedCount() {
        return statisticsGateway.cacheEvictedCount();
    }

    public double getCacheHitRatio() {
        return statisticsGateway.cacheHitRatio();
    }

}