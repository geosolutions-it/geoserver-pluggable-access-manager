package it.geosolutions.geoserver.security.config;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.geoserver.security.CatalogMode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class PluggableAccessManagerConfigurationTest {

    private static final String CONFIG_DEFAULTS = "/test-config-defaults.xml";

    private static final String CONFIG_MISSING_BUILDER = "/test-config-missing-builder.xml";

    private static final String CONFIG_MISSING_LAYER = "/test-config-missing-layer.xml";

    private static final String CONFIG_MISSING_WORKSPACE = "/test-config-missing-workspace.xml";

    private static final String CONFIG_WRONG_BUILDER_REF = "/test-config-wrong-builder-ref.xml";

    private static final String CONFIG_MISSING_ID = "/test-config-missing-id.xml";

    private static final String CONFIG_MISSING_BEAN_NAME = "/test-config-missing-bean-name.xml";

    private static final String CONFIG_OK = "/test-config.xml";

    private static XStream xstream;

    @BeforeClass
    public static void setUp() {
        xstream = PluggableAccessManagerConfigurator.buildXStream();
    }

    @Test
    public void testConfigDefaults() throws IOException {
        PluggableAccessManagerConfiguration conf = lookupConfig(CONFIG_DEFAULTS);
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

        // defaults will apply to any layer
        AccessLimitsBuilderConfiguration builder = conf.getLimitsBuilderConfiguration("whatever",
                "name");
        assertEquals(defaultBuilder.getId(), builder.getId());
        assertEquals(defaultBuilder.getBeanName(), builder.getBeanName());
        assertEquals(0, builder.getOptions().size());
    }

    @Test
    public void testConfigMissingBuilder() throws IOException {
        PluggableAccessManagerConfiguration conf = lookupConfig(CONFIG_MISSING_BUILDER);
        assertNotNull(conf);
        assertFalse(conf.isValid());
        assertEquals(1, conf.getRules().size());
        AccessRule invalidRule = conf.getRules().get(0);
        assertFalse(invalidRule.isValid());
        assertTrue(invalidRule.getAccessLimitsBuilder() == null);
        assertTrue(StringUtils.isNotBlank(invalidRule.getLayer()));
        assertTrue(StringUtils.isNotBlank(invalidRule.getWorkspace()));
    }

    @Test
    public void testConfigMissingLayer() throws IOException {
        PluggableAccessManagerConfiguration conf = lookupConfig(CONFIG_MISSING_LAYER);
        assertNotNull(conf);
        assertFalse(conf.isValid());
        assertEquals(1, conf.getRules().size());
        AccessRule invalidRule = conf.getRules().get(0);
        assertFalse(invalidRule.isValid());
        assertTrue(invalidRule.getLayer() == null);
        assertTrue(StringUtils.isNotBlank(invalidRule.getWorkspace()));
        assertTrue(StringUtils.isNotBlank(invalidRule.getAccessLimitsBuilder()));
    }

    @Test
    public void testConfigMissingWorkspace() throws IOException {
        PluggableAccessManagerConfiguration conf = lookupConfig(CONFIG_MISSING_WORKSPACE);
        assertNotNull(conf);
        assertFalse(conf.isValid());
        assertEquals(1, conf.getRules().size());
        AccessRule invalidRule = conf.getRules().get(0);
        assertFalse(invalidRule.isValid());
        assertTrue(invalidRule.getWorkspace() == null);
        assertTrue(StringUtils.isNotBlank(invalidRule.getLayer()));
        assertTrue(StringUtils.isNotBlank(invalidRule.getAccessLimitsBuilder()));
    }

    @Test
    public void testConfigWrongBuilderRef() throws IOException {
        PluggableAccessManagerConfiguration conf = lookupConfig(CONFIG_WRONG_BUILDER_REF);
        assertNotNull(conf);
        assertFalse(conf.isValid());
        assertEquals(1, conf.getRules().size());
        AccessRule invalidRule = conf.getRules().get(0);
        assertTrue(invalidRule.isValid()); // the rule per se is valid
        assertEquals(0, conf.getBuildersById().size()); // internal state is not populated if configuration is invalid
    }

    @Test
    public void testConfigMissingId() throws IOException {
        PluggableAccessManagerConfiguration conf = lookupConfig(CONFIG_MISSING_ID);
        assertNotNull(conf);
        assertFalse(conf.isValid());
        assertEquals(2, conf.getAccessLimitsBuilders().size());
        AccessLimitsBuilderConfiguration invalidBuilderConf = conf.getAccessLimitsBuilders().get(0);
        assertFalse(invalidBuilderConf.isValid());
        assertTrue(invalidBuilderConf.getId() == null);
        assertTrue(StringUtils.isNotBlank(invalidBuilderConf.getBeanName()));
        assertTrue(conf.getAccessLimitsBuilders().get(1).isValid());
    }

    @Test
    public void testConfigMissingBeanName() throws IOException {
        PluggableAccessManagerConfiguration conf = lookupConfig(CONFIG_MISSING_BEAN_NAME);
        assertNotNull(conf);
        assertFalse(conf.isValid());
        assertEquals(2, conf.getAccessLimitsBuilders().size());
        AccessLimitsBuilderConfiguration invalidBuilderConf = conf.getAccessLimitsBuilders().get(1);
        assertFalse(invalidBuilderConf.isValid());
        assertTrue(invalidBuilderConf.getBeanName() == null);
        assertTrue(StringUtils.isNotBlank(invalidBuilderConf.getId()));
        assertTrue(conf.getAccessLimitsBuilders().get(0).isValid());
    }

    @Test
    public void testConfigOk() throws IOException {
        PluggableAccessManagerConfiguration conf = lookupConfig(CONFIG_OK);
        assertNotNull(conf);
        assertTrue(conf.isValid());
        assertEquals(4, conf.getRules().size());
        assertEquals(3, conf.getAccessLimitsBuilders().size());

        // most specific rule wins
        AccessLimitsBuilderConfiguration toppStatesBuilderConf = conf
                .getLimitsBuilderConfiguration("topp", "states");
        assertNotNull(toppStatesBuilderConf);
        assertEquals("cql", toppStatesBuilderConf.getId());
        assertEquals("cqlFilterAccessLimitsBuilder", toppStatesBuilderConf.getBeanName());
        assertEquals(1, toppStatesBuilderConf.getOptions().size());
        assertEquals("FID IN ({0})", toppStatesBuilderConf.getOptions().get("cql_filter"));

        // rule appearing last wins
        AccessLimitsBuilderConfiguration toppLandmarksBuilderConf = conf
                .getLimitsBuilderConfiguration("topp", "landmarks");
        assertNotNull(toppLandmarksBuilderConf);
        assertEquals("no-opt", toppLandmarksBuilderConf.getId());
        assertEquals("noOptionsAccessLimitsBuilder", toppLandmarksBuilderConf.getBeanName());
        assertEquals(0, toppLandmarksBuilderConf.getOptions().size());

        // match layer name in any workspace
        AccessLimitsBuilderConfiguration tazmaniaRiversBuilderConf = conf
                .getLimitsBuilderConfiguration("tazmania", "rivers");
        assertNotNull(tazmaniaRiversBuilderConf);
        assertEquals("no-opt", tazmaniaRiversBuilderConf.getId());
        assertEquals("noOptionsAccessLimitsBuilder", tazmaniaRiversBuilderConf.getBeanName());
        assertEquals(0, tazmaniaRiversBuilderConf.getOptions().size());

        // default rule
        AccessLimitsBuilderConfiguration notMachingBuilderConf = conf
                .getLimitsBuilderConfiguration("whatever", "name");
        assertEquals("default", notMachingBuilderConf.getId());
        assertEquals("defaultAccessLimitsBuilder", notMachingBuilderConf.getBeanName());
        assertEquals(2, notMachingBuilderConf.getOptions().size());
        assertEquals("value_1", notMachingBuilderConf.getOptions().get("option_1"));
        assertEquals("2", notMachingBuilderConf.getOptions().get("option_2"));

        // catalog mode
        assertEquals(CatalogMode.CHALLENGE, conf.getCatalogMode());
    }

    private PluggableAccessManagerConfiguration lookupConfig(String configLocation)
            throws IOException {
        try (InputStream in = getClass().getResourceAsStream(configLocation)) {
            return (PluggableAccessManagerConfiguration) xstream.fromXML(in);
        }
    }

}
