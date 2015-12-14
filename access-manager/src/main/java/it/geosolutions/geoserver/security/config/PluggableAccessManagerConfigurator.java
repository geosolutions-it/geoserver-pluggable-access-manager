package it.geosolutions.geoserver.security.config;

import it.geosolutions.geoserver.security.impl.PluggableAccessManager;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.config.util.SecureXStream;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.Resource.Type;
import org.geoserver.platform.resource.ResourceListener;
import org.geoserver.platform.resource.ResourceNotification;
import org.geotools.util.logging.Logging;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;

/**
 * A configurator bean to notify the provided {@link PluggableAccessManager} instance of configuration changes.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class PluggableAccessManagerConfigurator implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOGGER = Logging
            .getLogger(PluggableAccessManagerConfiguration.class);

    /**
     * Configuration file path, relative to $GEOSERVER_DATA_DIR.
     */
    static final String CONFIG_FILE_PATH = "security/pluggable-access-manager.xml";

    /**
     * Bean name of the default access limits builder used by the default configuration.
     */
    static final String DEFAULT_ACCESS_LMITS_BUILDER = "allowAllAccessLimitsBuilder";

    XStream xstream;

    /** The default configuration (lazily initialized). */
    PluggableAccessManagerConfiguration defaultConfiguration;

    /** The configuration file as a {@link Resource}. */
    Resource configFile;

    /** Resource listener to trigger a configuration reload when the configuration file is updated. */
    ResourceListener listener = new ResourceListener() {
        public void changed(ResourceNotification notify) {
            loadConfiguration();
        }
    };

    /** The pluggable access manager to manage. */
    PluggableAccessManager accessManager;

    /**
     * Creates a {@link PluggableAccessManagerConfigurator} instance to manage the provided {@link PluggableAccessManager}, loads the configuration
     * and starts a watcher to be notified of changes to the configuration file.
     * 
     * @param accessManager the pluggable access manager to manage
     */
    public PluggableAccessManagerConfigurator(PluggableAccessManager accessManager) {
        this.accessManager = accessManager;
        xstream = buildXStream();

        GeoServerResourceLoader loader = GeoServerExtensions.bean(GeoServerResourceLoader.class);
        configFile = loader.get(CONFIG_FILE_PATH);
        loadConfiguration();
        configFile.addListener(listener);
    }

    /**
     * Loads the configuration from {@code $GEOSERVER_DATA_DIR/security/pluggable-access-manager.xml}.
     * 
     * <p>
     * If no configuration file can be found, uses internal defaults (i.e. allow all access).
     * </p>
     */
    void loadConfiguration() {
        PluggableAccessManagerConfiguration configuration = null;
        try {
            if (configFile.getType() == Type.RESOURCE) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Loading access manager configuration from file "
                            + configFile.name());
                }
                try (InputStream in = configFile.in()) {
                    configuration = (PluggableAccessManagerConfiguration) xstream.fromXML(in);
                    if (!configuration.isValid()) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("Configuration loaded from file " + configFile.name()
                                    + " is invalid: using internal defaults");
                        }
                        configuration = null;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading the configuration file " + configFile.name(), e);
            configuration = null;
        }
        if (configuration == null) {
            LOGGER.log(Level.INFO,
                    "Could not find/load the configuration file, using internal defaults");
            buildDefaultConfiguration();
            configuration = defaultConfiguration;
        }
        accessManager.setConfiguration(configuration);
    }

    void buildDefaultConfiguration() {
        if (defaultConfiguration == null) {
            this.defaultConfiguration = new PluggableAccessManagerConfiguration();
        }
    }

    /**
     * Builds and configures the {@link XStream} used for de-serializing the configuration.
     * 
     * @return a properly configured {@link XStream} instance
     */
    public static XStream buildXStream() {
        XStream xstream = new SecureXStream();
        xstream.alias("config", PluggableAccessManagerConfiguration.class);
        xstream.alias("builder", AccessLimitsBuilderConfiguration.class);
        xstream.alias("rule", AccessRule.class);
        xstream.useAttributeFor(AccessLimitsBuilderConfiguration.class, "id");
        xstream.allowTypes(new Class[] { PluggableAccessManagerConfiguration.class,
                AccessLimitsBuilderConfiguration.class, AccessRule.class });
        NamedMapConverter optionsConverter = new NamedMapConverter(xstream.getMapper(), "option",
                "name", String.class, null, String.class, true, false, xstream.getConverterLookup());
        xstream.registerConverter(optionsConverter);

        return xstream;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (configFile != null) {
            configFile.removeListener(listener);
        }
    }

}
