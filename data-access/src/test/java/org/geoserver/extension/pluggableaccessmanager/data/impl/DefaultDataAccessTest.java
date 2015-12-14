package org.geoserver.extension.pluggableaccessmanager.data.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.geoserver.extension.pluggableaccessmanager.data.DataAccess;
import org.geoserver.extension.pluggableaccessmanager.data.OnlineTestCase;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.logging.Logging;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Based on provided MS SQL Server database, which is required for the test to actually run (and pass).
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class DefaultDataAccessTest extends OnlineTestCase {

    private static final Logger LOGGER = Logging.getLogger(DefaultDataAccessTest.class);

    JDBCDataStore jdbc;

    DataAccess dataAccess;

    @Override
    protected boolean isOnline() throws Exception {
        DataStore store = DataStoreFinder.getDataStore(fixture);
        if (store == null) {
            LOGGER.log(Level.WARNING,
                    "Skipping tests, no datastore was found with connection parameters " + fixture);
            return false;
        }

        try {
            if (!(store instanceof JDBCDataStore)) {
                LOGGER.log(Level.WARNING,
                        "Skipping tests, the datastore found with connection parameters is not a JDBCDataStore: "
                                + fixture);
                return false;
            }

            // just make sure we can list tables
            store.getTypeNames();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not retrieve type names from data store", e);
            return false;
        } finally {
            store.dispose();
        }

        return true;
    }

    @Before
    public void setupDataAccess() throws Exception {
        jdbc = (JDBCDataStore) DataStoreFinder.getDataStore(fixture);

        // populate DB with fake data
        // TODO: populate CLIENT, USERS, R_CLIENT_IP tables
        clearData(jdbc);
        insertData(jdbc);

        dataAccess = new DefaultDataAccess(jdbc);
    }

    @After
    public void clearDataAccess() throws Exception {
        if (jdbc != null) {
            clearData(jdbc);
        }
        if (this.dataAccess != null) {
            this.dataAccess.dispose();
        }
    }

    private void clearData(JDBCDataStore jdbc) {
        executeUpdate(jdbc, "DELETE FROM service_access WHERE USER = 'geoserver@test_user'");
    }

    private void insertData(JDBCDataStore jdbc) {
        executeUpdate(jdbc, "INSERT INTO service_access(SERVICE_ID, USER) VALUES ('geoserver@test_service', 'geoserver@test_user')");
    }

    private void executeUpdate(JDBCDataStore jdbc, String sql) {
        Transaction t = new DefaultTransaction();
        try (Connection conn = jdbc.getConnection(t); Statement st = conn.createStatement()) {
            // clear table
            st.executeUpdate(sql);
            t.commit();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Override
    protected String getFixtureId() {
        return "data-access";
    }

    @Override
    protected Properties createExampleFixture() {
        Properties fixture = new Properties();

        try (InputStream fixtureStream = getClass().getResourceAsStream(
                DefaultDataAccessProvider.DEFAULT_CONFIG_FILE)) {
            fixture.load(fixtureStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create example fixture", e);
        }

        return fixture;
    }

    @Test
    public void testGetUserModules() throws IOException {
        assertTrue(dataAccess.getUserPermissions("doesnotexist").isEmpty());
        assertEquals("43,607,601,605,666",
                StringUtils.join(dataAccess.getUserPermissions("sea.subscriber"), ","));
    }

}
