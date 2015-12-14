package org.geoserver.extension.pluggableaccessmanager.data.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.geoserver.extension.pluggableaccessmanager.data.DataAccess;
import org.geoserver.extension.pluggableaccessmanager.data.DataAccessProvider;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.logging.Logging;

/**
 * {@link DataAccessProvider} implementation acting as a factory for {@link DefaultDataAccess}.
 * 
 * <p>
 * The underlying {@link JDBCDataStore} is configured via a regular GeoTools DataStore configuration file.
 * </p>
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class DefaultDataAccessProvider implements DataAccessProvider {

    private static final Logger LOGGER = Logging.getLogger(DefaultDataAccessProvider.class);

    /** Default configuration file name, relative to configuration directory. */
    static final String DEFAULT_CONFIG_FILE_NAME = "pluggableaccessmanager/data-access.properties";

    /** Default configuration file, loaded from the classpath. */
    static final String DEFAULT_CONFIG_FILE = "/org/geoserver/extension/pluggableaccessmanager/data/data-access.properties.default";

    /** The directory where the configuration file is located. */
    File configDir;

    /** The configuration file name. */
    String configFileName;

    /** The data access instance (singleton). */
    DataAccess dataAccess;

    /**
     * Single-argument constructor.
     * 
     * @param configDir the directory where the configuration file is located
     */
    DefaultDataAccessProvider(File configDir) {
        this(configDir, null);
    }

    /**
     * Two-argument constructor.
     * 
     * @param configDir the directory where the configuration file is located
     * @param configFileName the configuration file name, relative to the configuration directory
     */
    DefaultDataAccessProvider(File configDir, String configFileName) {
        this.configDir = configDir;
        this.configFileName = configFileName;
        // make sure configFileName is never null nor empty
        if (this.configFileName == null || this.configFileName.isEmpty()) {
            this.configFileName = DEFAULT_CONFIG_FILE_NAME;
        }
    }

    /**
     * Instantiates the data access.
     * 
     * <p>
     * The method calls {@link DataStoreFinder#getDataStore(java.util.Map)}, passing the properties read from the configuration file.
     * </p>
     * 
     * <p>
     * If no configuration file can be found at the specified location, an attempt will be made to create a default one and use that.
     * </p>
     * 
     * @throws IOException
     * @throws IllegalArgumentException if the configured DataStore is not a JDBCDataStore
     */
    void initDataAccess() throws IOException {
        if (configDir == null || !configDir.exists()) {
            throw new IllegalArgumentException("Provided configuration directory does not exist");
        }

        File configFile = new File(configDir, configFileName);
        if (!configFile.exists()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Configuration file not found at " + configFile.getAbsolutePath()
                        + ": using defaults");
            }
            createDefaultConfig(configFile);
        }

        Properties config = new Properties();
        try (InputStream configStream = new FileInputStream(configFile)) {
            config.load(configStream);
        }
        DataStore dataStore = DataStoreFinder.getDataStore(config);
        if (dataStore instanceof JDBCDataStore) {
            this.dataAccess = new DefaultDataAccess((JDBCDataStore) dataStore);
        } else {
            throw new IllegalArgumentException("Configured DataStore is not a JDBCDataStore");
        }
    }

    private void createDefaultConfig(File configFile) throws IOException {
        InputStream input = getClass().getResourceAsStream(DEFAULT_CONFIG_FILE);
        try (InputStreamReader reader = new InputStreamReader(input, "UTF-8");
                FileOutputStream os = new FileOutputStream(configFile)) {
            IOUtils.copy(reader, os);
        }
    }

    /**
     * If no data access instance has been created yet, it creates a new one; otherwise, the existing instance is returned.
     * 
     * @see DataAccessProvider#getDataAccess()
     */
    @Override
    public DataAccess getDataAccess() throws IOException {
        synchronized (this) {
            if (dataAccess == null) {
                initDataAccess();
            }
        }
        return dataAccess;
    }

}
