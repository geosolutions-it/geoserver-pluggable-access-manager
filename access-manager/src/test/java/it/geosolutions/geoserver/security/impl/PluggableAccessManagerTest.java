package it.geosolutions.geoserver.security.impl;

import static it.geosolutions.geoserver.security.impl.PluggableAccessManagerTestUtils.copyConfigurationFile;
import static it.geosolutions.geoserver.security.impl.PluggableAccessManagerTestUtils.getLoggedInUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.geoserver.security.config.AccessLimitsBuilderConfiguration;
import it.geosolutions.geoserver.security.config.AccessRule;
import it.geosolutions.geoserver.security.config.PluggableAccessManagerConfiguration;

import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.data.test.CiteTestData;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.security.CatalogMode;
import org.geoserver.security.CoverageAccessLimits;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.VectorAccessLimits;
import org.geoserver.security.WorkspaceAccessLimits;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.geotools.feature.NameImpl;
import org.junit.Test;
import org.opengis.filter.Filter;

public class PluggableAccessManagerTest extends GeoServerSystemTestSupport {

    private static final String CONFIG_FILE = "/test-config-default-deny.xml";

    private static final String ALLOW_ALL_BUILDER_ID = "allow-all";

    private static final CatalogMode CATALOG_MODE = CatalogMode.CHALLENGE;

    @Override
    protected void setUpTestData(SystemTestData testData) throws Exception {
        super.setUpTestData(testData);
        testData.setUpDefaultRasterLayers();
        testData.setUpWcs10RasterLayers();

        copyConfigurationFile(testData.getDataDirectoryRoot(), CONFIG_FILE);
    }

    @Test
    public void testLoadConfiguration() {
        PluggableAccessManager accessManager = applicationContext.getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        PluggableAccessManagerConfiguration conf = accessManager.getConfiguration();

        assertNotNull(conf);
        assertTrue(conf.isValid());
        assertEquals(1, conf.getAccessLimitsBuilders().size());
        AccessLimitsBuilderConfiguration allowAllBuilderConf = conf.getAccessLimitsBuilders()
                .get(0);
        assertNotNull(allowAllBuilderConf);
        assertEquals(ALLOW_ALL_BUILDER_ID, allowAllBuilderConf.getId());
        assertEquals("allowAllAccessLimitsBuilder", allowAllBuilderConf.getBeanName());
        assertEquals(2, conf.getRules().size());
        AccessRule citeBuildingsRule = conf.getRules().get(0);
        assertNotNull(citeBuildingsRule);
        assertEquals("cite.Buildings", citeBuildingsRule.getKey());
        assertEquals(ALLOW_ALL_BUILDER_ID, citeBuildingsRule.getAccessLimitsBuilder());
        AccessRule wcsWorkspaceRule = conf.getRules().get(1);
        assertNotNull(wcsWorkspaceRule);
        assertEquals("wcs.*", wcsWorkspaceRule.getKey());
        assertEquals(ALLOW_ALL_BUILDER_ID, wcsWorkspaceRule.getAccessLimitsBuilder());
        // default config
        assertEquals("denyAllAccessLimitsBuilder", conf.getDefaultAccessLimitsBuilder()
                .getBeanName());
    }

    @Test
    public void testAccessLimitsBuilderCache() {
        PluggableAccessManager accessManager = applicationContext.getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        // empty cache first
        accessManager.accessLimitsBuildersCache.clear();
        accessManager.numLookups = 0;

        LayerInfo buildings = getCatalog().getLayerByName(new NameImpl(CiteTestData.BUILDINGS));
        // trigger a lookup of bean allowAllAccessLimitsBuilder from Spring's context
        DataAccessLimits limits = accessManager.getAccessLimits(getLoggedInUser(), buildings);
        assertEquals(1, accessManager.numLookups);
        LayerInfo dem = getCatalog().getLayerByName(new NameImpl(CiteTestData.TASMANIA_DEM));
        // this call should NOT trigger another lookup, as the same access limit builder applies
        limits = accessManager.getAccessLimits(getLoggedInUser(), dem);
        assertEquals(1, accessManager.numLookups);
        LayerInfo bridges = getCatalog().getLayerByName(new NameImpl(CiteTestData.BRIDGES));
        // a second lookup is necessary here, to fetch bean denyAllAccessLimitsBuilder from context
        limits = accessManager.getAccessLimits(getLoggedInUser(), bridges);
        assertEquals(2, accessManager.numLookups);
    }

    @Test
    public void testLayerAccessDenied() {
        PluggableAccessManager accessManager = applicationContext.getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        loginAsRegularUser();
        // only vector layer allowed is cite:Buildings, so cite:Bridges should not be accessible
        LayerInfo bridges = getCatalog().getLayerByName(new NameImpl(CiteTestData.BRIDGES));
        DataAccessLimits limits = accessManager.getAccessLimits(getLoggedInUser(), bridges);
        assertAccessDenied(limits, true);
        // raster layer, not in wcs workspace
        LayerInfo usa = getCatalog().getLayerByName(new NameImpl(CiteTestData.USA_WORLDIMG));
        limits = accessManager.getAccessLimits(getLoggedInUser(), usa);
        assertAccessDenied(limits, false);
    }

    @Test
    public void testLayerAccessAllowed() {
        PluggableAccessManager accessManager = applicationContext.getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        loginAsRegularUser();
        // only vector layer allowed is cite:Buildings
        LayerInfo buildings = getCatalog().getLayerByName(new NameImpl(CiteTestData.BUILDINGS));
        DataAccessLimits limits = accessManager.getAccessLimits(getLoggedInUser(), buildings);
        assertAccessAllowed(limits, true);
        // raster layer in wcs workspace --> allowed
        LayerInfo dem = getCatalog().getLayerByName(new NameImpl(CiteTestData.TASMANIA_DEM));
        limits = accessManager.getAccessLimits(getLoggedInUser(), dem);
        assertAccessAllowed(limits, false);
    }

    @Test
    public void testAdminAccess() {
        PluggableAccessManager accessManager = applicationContext.getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        loginAsAdmin();
        // only vector layer allowed is cite:Buildings, so cite:Bridges should NOT be accessible, but admin can do anything!
        LayerInfo bridges = getCatalog().getLayerByName(new NameImpl(CiteTestData.BRIDGES));
        DataAccessLimits limits = accessManager.getAccessLimits(getLoggedInUser(), bridges);
        assertAccessAllowed(limits, true);
        // raster layer not in wcs workspace, so it should NOT be accessible, but admin can do anything!
        LayerInfo usa = getCatalog().getLayerByName(new NameImpl(CiteTestData.USA_WORLDIMG));
        limits = accessManager.getAccessLimits(getLoggedInUser(), usa);
        assertAccessAllowed(limits, false);
        // check the obvious, i.e. layers accessible to anybody can be accessed by admin
        LayerInfo buildings = getCatalog().getLayerByName(new NameImpl(CiteTestData.BUILDINGS));
        limits = accessManager.getAccessLimits(getLoggedInUser(), buildings);
        assertAccessAllowed(limits, true);
        LayerInfo dem = getCatalog().getLayerByName(new NameImpl(CiteTestData.TASMANIA_DEM));
        limits = accessManager.getAccessLimits(getLoggedInUser(), dem);
        assertAccessAllowed(limits, false);
    }

    @Test
    public void testWorkspaceAccess() {
        PluggableAccessManager accessManager = applicationContext.getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);

        loginAsRegularUser();

        WorkspaceInfo cite = getCatalog().getWorkspaceByName("cite");
        WorkspaceAccessLimits limits = accessManager.getAccessLimits(getLoggedInUser(), cite);
        assertNotNull(limits);
        assertEquals(CATALOG_MODE, limits.getMode());
        // regular users can just read
        assertTrue(limits.isReadable());
        assertFalse(limits.isWritable());
        assertFalse(limits.isAdminable());

        logout();
        loginAsAdmin();

        limits = accessManager.getAccessLimits(getLoggedInUser(), cite);
        assertNotNull(limits);
        assertEquals(CATALOG_MODE, limits.getMode());
        // admin can do anything!
        assertTrue(limits.isReadable());
        assertTrue(limits.isWritable());
        assertTrue(limits.isAdminable());
    }

    private void assertAccessDenied(DataAccessLimits limits, boolean isVector) {
        assertNotNull(limits);
        assertEquals(CATALOG_MODE, limits.getMode());
        if (isVector) {
            assertTrue(limits instanceof VectorAccessLimits);
            VectorAccessLimits vectorLimits = (VectorAccessLimits) limits;
            assertEquals(Filter.EXCLUDE, vectorLimits.getReadFilter());
            assertTrue(vectorLimits.getReadAttributes() == null);
            assertEquals(Filter.EXCLUDE, vectorLimits.getWriteFilter());
            assertTrue(vectorLimits.getWriteAttributes() == null);
        } else {
            assertTrue(limits instanceof CoverageAccessLimits);
            CoverageAccessLimits rasterLimits = (CoverageAccessLimits) limits;
            assertEquals(Filter.EXCLUDE, rasterLimits.getReadFilter());
            assertTrue(rasterLimits.getParams() == null);
            assertTrue(rasterLimits.getRasterFilter() == null);
        }
    }

    private void assertAccessAllowed(DataAccessLimits limits, boolean isVector) {
        assertNotNull(limits);
        assertEquals(CATALOG_MODE, limits.getMode());
        if (isVector) {
            assertTrue(limits instanceof VectorAccessLimits);
            VectorAccessLimits vectorLimits = (VectorAccessLimits) limits;
            assertEquals(Filter.INCLUDE, vectorLimits.getReadFilter());
            assertTrue(vectorLimits.getReadAttributes() == null);
            assertEquals(Filter.INCLUDE, vectorLimits.getWriteFilter());
            assertTrue(vectorLimits.getWriteAttributes() == null);
        } else {
            assertTrue(limits instanceof CoverageAccessLimits);
            CoverageAccessLimits rasterLimits = (CoverageAccessLimits) limits;
            assertEquals(Filter.INCLUDE, rasterLimits.getReadFilter());
            assertTrue(rasterLimits.getParams() == null);
            assertTrue(rasterLimits.getRasterFilter() == null);
        }
    }

    private void loginAsRegularUser() {
        login("bob", "password");
    }

    private void loginAsAdmin() {
        login("admin", "password", GeoServerRole.ADMIN_ROLE.getAuthority());
    }

}
