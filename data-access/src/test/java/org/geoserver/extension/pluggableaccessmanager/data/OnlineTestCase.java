/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2010, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geoserver.extension.pluggableaccessmanager.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.geotools.test.FixtureUtilities;
import org.junit.Assume;
import org.junit.Before;

/**
 * Class derived from GeoTools {@link org.geotools.test.OnlineTestCase}, using JUnit 4.x testing
 * facilities instead of 3.x ones, and looking for database connection files in
 * <code>$HOME/.pluggableaccessmanager</code> instead of <code>$HOME/.geotools</code>
 * 
 * @author Stefano Costa, GeoSolutions
 */
public abstract class OnlineTestCase {
    /**
     * A static map which tracks which fixtures are offline. This prevents continually trying to 
     * run a test when an external resource is offline.  
     */
    protected static Map<String,Boolean> online = new HashMap<String,Boolean>();
    
    /**
     * A static map which tracks which fixture files can not be found. This prevents
     * continually looking up the file and reporting it not found to the user.
     */
    protected static Map<String,Boolean> found = new HashMap<String,Boolean>();

    /**
     * The test fixture, {@code null} if the fixture is not available.
     */
    protected Properties fixture;

    @Before
    public void checkOnline() throws Exception {
        // check we are online, disable the tests otherwise
        configureFixture();
        Assume.assumeTrue(isOnline());
    }
    

    /**
     * Check whether the fixture is available. This method also loads the configuration if present,
     * and tests the connection using {@link #isOnline()}.
     * 
     * @return true if fixture is available for use
     */
    boolean checkAvailable() {
        configureFixture();
        if (fixture == null) {
            return false;
        } else {
            String fixtureId = getFixtureId();
            // do an online/offline check
            Boolean available = online.get(fixtureId);
            if (available == null) {
                // test the connection
                try {
                    available = isOnline();
                } catch (Throwable t) {
                    System.out.println("Skipping " + fixtureId
                            + " tests, resources not available: " + t.getMessage());
                    t.printStackTrace();
                    available = Boolean.FALSE;
                }
                online.put(fixtureId, available);
            }
            return available;
        }
    }

    /**
     * Load fixture configuration. Create example if absent.
     */
    private void configureFixture() {
        if (fixture == null) {
            String fixtureId = getFixtureId();
            if (fixtureId == null) {
                return; // not available (turn test off)
            }
            try {
                // load the fixture
                File base = getFixtureBase();
                File fixtureFile = FixtureUtilities.getFixtureFile(base, fixtureId);
                Boolean exists = found.get(fixtureFile.getCanonicalPath());
                if (exists == null || exists.booleanValue()) {
                    if (fixtureFile.exists()) {
                        fixture = FixtureUtilities.loadProperties(fixtureFile);
                        found.put(fixtureFile.getCanonicalPath(), true);
                    } else {
                        // no fixture file, if no profile was specified write out a template
                        // fixture using the offline fixture properties
                        Properties exampleFixture = createExampleFixture();
                        if (exampleFixture != null) {
                            File exFixtureFile = new File(fixtureFile.getAbsolutePath()
                                    + ".example");
                            if (!exFixtureFile.exists()) {
                                createExampleFixture(exFixtureFile, exampleFixture);
                            }
                        }
                        found.put(fixtureFile.getCanonicalPath(), false);
                    }
                }
                if (fixture == null) {
                    fixture = createOfflineFixture();
                }
                if (fixture == null && exists == null) {
                    // only report if exists == null since it means that this is
                    // the first time trying to load the fixture
                    FixtureUtilities.printSkipNotice(fixtureId, fixtureFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    void createExampleFixture(File exFixtureFile, Properties exampleFixture) {
        try {
            exFixtureFile.getParentFile().mkdirs();
            exFixtureFile.createNewFile();
            
            FileOutputStream fout = new FileOutputStream(exFixtureFile);
        
            exampleFixture.store(fout, "This is an example fixture. Update the " +
                "values and remove the .example suffix to enable the test"); 
            fout.flush();
            fout.close();
            System.out.println("Wrote example fixture file to " + exFixtureFile);
        }
        catch(IOException ioe) {
            System.out.println("Unable to write out example fixture " + exFixtureFile); 
            ioe.printStackTrace();
        }
    }

    
    /**
     * Tests if external resources needed to run the tests are online.
     * <p>
     * This method can return false to indicate the online resources are not up, or can simply
     * throw an exception. 
     * </p>
     * @return True if external resources are online, otherwise false.
     * @throws Exception Any errors that occur determining if online resources are available.
     */
    protected abstract boolean isOnline() throws Exception;
    

    /**
     * Allows tests to create an offline fixture in cases where the user has not
     * specified an explicit fixture for the test.
     * <p>
     * Note, that this should method should on be implemented if the test case
     * is created of creating a fixture which relies soley on embedded or offline
     * resources. It should not reference any external or online resources as it
     * prevents the user from running offline. 
     * </p>
     */
    protected Properties createOfflineFixture() {
        return null;
    }
    
    /**
     * Allows test to create a sample fixture for users. 
     * <p>
     * If this method returns a value the first time a fixture is looked up and not 
     * found this method will be called to create a fixture file with teh same id, but 
     * suffixed with .template.
     * </p>
     */
    protected Properties createExampleFixture() {
        return null;
    }
    
    /**
     * The fixture id for the test case.
     * <p>
     * This name is hierarchical, similar to a java package name. Example:
     * {@code "postgis.demo_bc"}.
     * </p>
     * 
     * @return The fixture id.
     */
    protected abstract String getFixtureId();
    
    /**
     * Returns the base directory containing the fixture files
     * @return
     */
    protected File getFixtureBase() {
        return new File(System.getProperty("user.home") + File.separator + ".pluggableaccessmanager");
    }
}
