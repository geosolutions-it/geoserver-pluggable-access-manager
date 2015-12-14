package org.geoserver.extension.pluggableaccessmanager.security.impl;

import static it.geosolutions.geoserver.security.impl.PluggableAccessManagerTestUtils.copyConfigurationFile;
import static it.geosolutions.geoserver.security.impl.PluggableAccessManagerTestUtils.getLoggedInUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.geoserver.security.config.PluggableAccessManagerConfiguration;
import it.geosolutions.geoserver.security.config.PluggableAccessManagerConfigurator;
import it.geosolutions.geoserver.security.impl.PluggableAccessManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.geoserver.catalog.LayerInfo;
import org.geoserver.data.test.CiteTestData;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.VectorAccessLimits;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.geotools.feature.NameImpl;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.junit.Test;
import org.opengis.filter.Filter;

import com.thoughtworks.xstream.XStream;

public class DataAccessLimitsBuilderTest extends GeoServerSystemTestSupport {

    private static final String CONFIG_FILE = "/test-config-alb.xml";

    private static final String CONFIG_FILE_NO_OPTIONS = "/test-config-alb-no-options.xml";

    @Override
    protected void setUpTestData(SystemTestData testData) throws Exception {
        super.setUpTestData(testData);

        copyConfigurationFile(testData.getDataDirectoryRoot(), CONFIG_FILE);
    }

    @Override
    protected void setUpSpring(List<String> springContextLocations) {
        super.setUpSpring(springContextLocations);

        springContextLocations.add("classpath:/testApplicationContext.xml");
    }

    @Test
    public void testLoadConfiguration() {
        PluggableAccessManager accessManager = applicationContext
                .getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        PluggableAccessManagerConfiguration conf = accessManager.getConfiguration();
        assertNotNull(conf);
        assertTrue(conf.isValid());
        assertEquals(2, conf.getAccessLimitsBuilders().size());
        assertEquals("test-builder", conf.getAccessLimitsBuilders().get(0).getId());
        assertEquals("allow-all", conf.getAccessLimitsBuilders().get(1).getId());
        assertEquals(3, conf.getRules().size());
        assertEquals("cite.RoadSegments", conf.getRules().get(0).getKey());
    }

    @Test
    public void testDefaultFilterTemplate() throws IOException, CQLException {
        InputStream configNoOptsInput = getClass().getResourceAsStream(CONFIG_FILE_NO_OPTIONS);
        XStream xstream = PluggableAccessManagerConfigurator.buildXStream();
        PluggableAccessManagerConfiguration configNoOpts = (PluggableAccessManagerConfiguration) xstream
                .fromXML(configNoOptsInput);
        assertNotNull(configNoOpts);
        assertTrue(configNoOpts.isValid());

        PluggableAccessManager accessManager = applicationContext
                .getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        // save current config to be restored at the end of the test
        PluggableAccessManagerConfiguration config = accessManager.getConfiguration();
        try {
            accessManager.setConfiguration(configNoOpts);

            // login as bob
            login("bob", "password");

            final Filter BOB_FILTER = ECQL.toFilter("service_id IN (105, 106)");
            LayerInfo roadSegments = getCatalog().getLayerByName(
                    new NameImpl(CiteTestData.ROAD_SEGMENTS));
            DataAccessLimits limits = accessManager.getAccessLimits(getLoggedInUser(), roadSegments);
            assertNotNull(limits);
            assertTrue(limits instanceof VectorAccessLimits);
            VectorAccessLimits vectorLimits = (VectorAccessLimits) limits;
            assertEquals(BOB_FILTER, vectorLimits.getReadFilter());
            assertNull(vectorLimits.getReadAttributes());
            assertEquals(BOB_FILTER, vectorLimits.getWriteFilter());
            assertNull(vectorLimits.getWriteAttributes());
        } finally {
            accessManager.setConfiguration(config);
        }
    }

    @Test
    public void testLayerAccess() throws CQLException {
        final Filter BOB_FILTER = ECQL.toFilter("FID IN (105,106)");
        final Filter ALICE_FILTER = ECQL.toFilter("FID IN (105)");

        PluggableAccessManager accessManager = applicationContext
                .getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);

        // login as bob
        login("bob", "password");

        // only vector layer allowed is cite:RoadSegments
        LayerInfo roadSegments = getCatalog().getLayerByName(
                new NameImpl(CiteTestData.ROAD_SEGMENTS));
        DataAccessLimits limits = accessManager.getAccessLimits(getLoggedInUser(), roadSegments);
        assertNotNull(limits);
        assertTrue(limits instanceof VectorAccessLimits);
        VectorAccessLimits vectorLimits = (VectorAccessLimits) limits;
        assertEquals(BOB_FILTER, vectorLimits.getReadFilter());
        assertNull(vectorLimits.getReadAttributes());
        assertEquals(BOB_FILTER, vectorLimits.getWriteFilter());
        assertNull(vectorLimits.getWriteAttributes());

        // login as alice
        logout();
        login("alice", "password");

        limits = accessManager.getAccessLimits(getLoggedInUser(), roadSegments);
        assertNotNull(limits);
        assertTrue(limits instanceof VectorAccessLimits);
        vectorLimits = (VectorAccessLimits) limits;
        assertEquals(ALICE_FILTER, vectorLimits.getReadFilter());
        assertNull(vectorLimits.getReadAttributes());
        assertEquals(ALICE_FILTER, vectorLimits.getWriteFilter());
        assertNull(vectorLimits.getWriteAttributes());

        // login as stranger
        logout();
        login("stranger", "password");

        limits = accessManager.getAccessLimits(getLoggedInUser(), roadSegments);
        assertNotNull(limits);
        assertTrue(limits instanceof VectorAccessLimits);
        vectorLimits = (VectorAccessLimits) limits;
        // stranger has no rights to see anything
        assertEquals(Filter.EXCLUDE, vectorLimits.getReadFilter());
        assertNull(vectorLimits.getReadAttributes());
        assertEquals(Filter.EXCLUDE, vectorLimits.getWriteFilter());
        assertNull(vectorLimits.getWriteAttributes());

        // login as admin
        logout();
        login("admin", "admin", GeoServerRole.ADMIN_ROLE.getAuthority());
        limits = accessManager.getAccessLimits(getLoggedInUser(), roadSegments);
        assertNotNull(limits);
        assertTrue(limits instanceof VectorAccessLimits);
        vectorLimits = (VectorAccessLimits) limits;
        // as usual, admin can do anything
        assertEquals(Filter.INCLUDE, vectorLimits.getReadFilter());
        assertNull(vectorLimits.getReadAttributes());
        assertEquals(Filter.INCLUDE, vectorLimits.getWriteFilter());
        assertNull(vectorLimits.getWriteAttributes());
    }
}
