/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestProperties.java,v $
 * Date   : $Date: 2006/08/24 06:43:24 $
 * Version: $Revision: 1.12.8.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.test;

import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPropertyUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;

/**
 * Reads and manages the test.properties file.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.12.8.1 $
 * 
 * @since 6.0.0
 */
public final class OpenCmsTestProperties {

    /** The log object for this class. */
    public static final Log LOG = CmsLog.getLog(OpenCmsTestProperties.class);

    /** The configuration from <code>opencms.properties</code>. */
    private static ExtendedProperties m_configuration;

    /** The singleton instance. */
    private static OpenCmsTestProperties m_testSingleton;

    /** The path to the test.properties file. */
    private String m_basePath;

    /** The database to use. */
    private String m_dbProduct;

    /** The path to the data test folder. */
    private String m_testDataPath;

    /** The path to the webapp test folder. */
    private String m_testWebappPath;

    /**
     * Private default constructor.
     */
    private OpenCmsTestProperties() {

        // noop
    }

    /**
     * @return the singleton instance
     */
    public static OpenCmsTestProperties getInstance() {

        if (m_testSingleton == null) {
            throw new RuntimeException("You have to initialize the test properties.");
        }
        return m_testSingleton;
    }

    /**
     * Returns the absolute path name for the given relative 
     * path name if it was found by the context Classloader of the 
     * current Thread.<p>
     * 
     * The argument has to denote a resource within the Classloaders 
     * scope. A <code>{@link java.net.URLClassLoader}</code> implementation for example would 
     * try to match a given path name to some resource under it's URL 
     * entries.<p>
     * 
     * As the result is internally obtained as an URL it is reduced to 
     * a file path by the call to <code>{@link java.net.URL#getFile()}</code>. Therefore 
     * the returned String will start with a '/' (no problem for java.io).<p>
     * 
     * @param fileName the filename to return the path from the Classloader for
     * 
     * @return the absolute path name for the given relative 
     *   path name if it was found by the context Classloader of the 
     *   current Thread or an empty String if it was not found
     * 
     * @see Thread#getContextClassLoader()
     */
    public static String getResourcePathFromClassloader(String fileName) {

        boolean isFolder = CmsResource.isFolder(fileName);
        String result = "";
        URL inputUrl = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (inputUrl != null) {
            // decode name here to avoid url encodings in path name
            result = CmsFileUtil.normalizePath(inputUrl);
            if (isFolder && !CmsResource.isFolder(result)) {
                result = result + '/';
            }
        } else {
            try {
                URLClassLoader cl = (URLClassLoader)Thread.currentThread().getContextClassLoader();
                URL[] paths = cl.getURLs();
                LOG.error(Messages.get().getBundle().key(
                    Messages.ERR_MISSING_CLASSLOADER_RESOURCE_2,
                    fileName,
                    Arrays.asList(paths)));
            } catch (Throwable t) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_MISSING_CLASSLOADER_RESOURCE_1, fileName));
            }
        }
        return result;
    }

    /**
     * Reads property file test.properties and fills singleton members.<p>
     * 
     * @param basePath the path where to find the test.properties file
     */
    public static void initialize(String basePath) {

        if (m_testSingleton != null) {
            return;
        }

        String testPropPath;
        m_testSingleton = new OpenCmsTestProperties();

        m_testSingleton.m_basePath = basePath;
        if (!m_testSingleton.m_basePath.endsWith("/")) {
            m_testSingleton.m_basePath += "/";
        }

        try {
            testPropPath = OpenCmsTestProperties.getResourcePathFromClassloader("test.properties");
            if (testPropPath == null) {
                throw new RuntimeException(
                    "Test property file ('test.properties') could not be found by context Classloader.");
            }
            File f = new File(testPropPath);
            if (!f.exists()) {
                throw new RuntimeException(
                    "Test property file ('test.properties') could not be found. Context Classloader suggested location: "
                        + testPropPath);
            }
            m_configuration = CmsPropertyUtils.loadProperties(testPropPath);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }

        m_testSingleton.m_testDataPath = m_configuration.getString("test.data.path");
        m_testSingleton.m_testWebappPath = m_configuration.getString("test.webapp.path");
        m_testSingleton.m_dbProduct = m_configuration.getString("db.product");

    }

    /**
     * @return Returns the path to the test.properties file
     */
    public String getBasePath() {

        return m_basePath;
    }

    /**
     * @return the parsed configuration file ('test.properties')
     */

    public ExtendedProperties getConfiguration() {

        return m_configuration;
    }

    /**
     * 
     * @return a String identifying the db.product property value of the 'test.properties' value.
     */
    public String getDbProduct() {

        return m_dbProduct;
    }

    /**
     * @return the path to the data test directory
     */
    public String getTestDataPath() {

        return m_testDataPath;
    }

    /**
     * @return the path to the webapp test directory
     */
    public String getTestWebappPath() {

        return m_testWebappPath;
    }
}