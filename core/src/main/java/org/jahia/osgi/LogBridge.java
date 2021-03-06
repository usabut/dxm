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
package org.jahia.osgi;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This LogBridge is used by a custom pax-logging-service appender to bring back up the log messages inside the OSGi runtime back into DX's
 * core.
 */
public class LogBridge {

    /**
     * Performs logging of the provided message and exception.
     *
     * @param loggerName the name of the logger
     * @param level the logging level
     * @param message the message to be logged
     * @param t the exception to be logged
     */
    public static void log(String loggerName, int level, Object message, Throwable t) {

        if (level == Level.OFF_INT) {
            return;
        }

        Logger logger = LoggerFactory.getLogger(loggerName);
        String msg = (message != null ? message.toString() : null);

        if (level >= Level.ERROR_INT) {
            logger.error(msg, t);
        } else if (level >= Level.WARN_INT) {
            logger.warn(msg, t);
        } else if (level >= Level.INFO_INT) {
            logger.info(msg, t);
        } else if (level >= Level.DEBUG_INT) {
            logger.debug(msg, t);
        } else {
            logger.trace(msg, t);
        }
    }
}
