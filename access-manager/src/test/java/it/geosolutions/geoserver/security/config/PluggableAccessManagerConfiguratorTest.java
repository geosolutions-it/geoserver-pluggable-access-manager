package it.geosolutions.geoserver.security.config;

import static it.geosolutions.geoserver.security.impl.PluggableAccessManagerTestUtils.CONFIG_FILE_DEST;
import static it.geosolutions.geoserver.security.impl.PluggableAccessManagerTestUtils.copyConfigurationFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.geoserver.security.impl.PluggableAccessManager;

import java.io.IOException;

import org.geoserver.data.test.SystemTestData;
import org.geoserver.platform.resource.Resource;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.junit.Test;

public class PluggableAccessManagerConfiguratorTest extends GeoServerSystemTestSupport {

    private static final String CONFIG_FILE_SOURCE = "/test-config.xml";

    @Override
    protected void setUpTestData(SystemTestData testData) throws Exception {
        super.setUpTestData(testData);

        copyConfigurationFile(testData.getDataDirectoryRoot(), CONFIG_FILE_SOURCE);
    }

    @Test
    public void testLoadConfiguration() throws IOException {
        PluggableAccessManager accessManager = applicationContext.getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        // configuration should have been loaded already
        PluggableAccessManagerConfiguration conf = accessManager.getConfiguration();
        assertNotNull(conf);
        assertTrue(conf.isValid());
        assertEquals(4, conf.getRules().size());
        assertEquals(3, conf.getAccessLimitsBuilders().size());
    }

    @Test
    public void testDefaults() throws IOException {
        Resource configFileResource = getDataDirectory().getSecurity(CONFIG_FILE_DEST);
        assertNotNull(configFileResource);

        PluggableAccessManagerConfigurator configurator = applicationContext
                .getBean(PluggableAccessManagerConfigurator.class);
        assertNotNull(configurator);
        // remove configuration file and manually trigger the loading of default configuration
        configFileResource.removeListener(configurator.listener);
        assertTrue(configFileResource.delete());
        configurator.loadConfiguration();

        PluggableAccessManager accessManager = applicationContext
                .getBean(PluggableAccessManager.class);
        assertNotNull(accessManager);
        // default configuration should have been loaded
        PluggableAccessManagerConfiguration conf = accessManager.getConfiguration();
        assertNotNull(conf);
        assertTrue(conf.isValid());
        assertEquals(0, conf.getAccessLimitsBuilders().size());
        assertEquals(0, conf.getAccessLimitsBuilders().size());
        assertEquals(PluggableAccessManagerConfiguration.DEFAULT_CATALOG_MODE,
                conf.getCatalogMode());
        AccessLimitsBuilderConfiguration defaultBuilder = conf.getDefaultAccessLimitsBuilder();
        assertNotNull(defaultBuilder);
        assertEquals(PluggableAccessManagerConfiguration.DEFAULT_CONFIG_ID, defaultBuilder.getId());
        assertEquals(PluggableAccessManagerConfiguration.DEFAULT_CONFIG_BEAN_NAME,
                defaultBuilder.getBeanName());
        assertEquals(0, defaultBuilder.getOptions().size());

    }

}
