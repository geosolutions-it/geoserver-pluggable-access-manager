package org.geoserver.extension.pluggableaccessmanager.ehcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.ehcache.CacheManager;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class EhCacheManagerFactoryBean implements FactoryBean<CacheManager>, InitializingBean,
        DisposableBean {

    private static final Logger LOGGER = Logger
            .getLogger(EhCacheManagerFactoryBean.class.getName());

    /** EhCache configuration file location - relative to the specified configuration directory. */
    public static final String EHCACHE_CONFIG_FILE = "pluggableaccessmanager/ehcache.xml";

    /** Classpath resource providing the default EhCache configuration file. */
    static final String EHCACHE_DEFAULT_CONFIG = "/org/geoserver/extension/pluggableaccessmanager/ehcache/ehcache-default.xml";

    /** EhCache configuration directory. */
    File configDir;

    /** EhCache cache manager (singleton). */
    CacheManager cacheManager;

    public EhCacheManagerFactoryBean(File configDir) {
        checkConfigDir(configDir);
        this.configDir = configDir;
    }

    private void checkConfigDir(File configDir) {
        if (configDir == null) {
            throw new IllegalArgumentException("No configuration directory specified");
        }
        if (!configDir.exists()) {
            throw new IllegalArgumentException(configDir.getAbsolutePath() + " does not exist");
        }
        if (!configDir.isDirectory() || !configDir.canWrite()) {
            throw new IllegalArgumentException(configDir.getAbsolutePath()
                    + " is not a directory or is not writable");
        }
    }

    InputStream loadConfiguration(File configDir) {
        InputStream configInput = null;

        File configFile = new File(configDir, EHCACHE_CONFIG_FILE);
        if (!configFile.exists()) {
            // try to create default configuration file
            try {
                FileUtils.copyInputStreamToFile(getDefaultConfig(), configFile);
            } catch (IOException e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Could not create configuration file", e);
                }
                // read config from classpath resource
                configInput = getDefaultConfig();
            }
        }

        if (configInput == null) {
            try {
                configInput = new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Could not read configuration file", e);
                }
                // read config from classpath resource
                configInput = getDefaultConfig();
            }
        }

        return configInput;
    }

    InputStream getDefaultConfig() {
        return getClass().getResourceAsStream(EHCACHE_DEFAULT_CONFIG);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try (InputStream config = loadConfiguration(configDir)) {
            cacheManager = CacheManager.create(config);
        }
    }

    @Override
    public CacheManager getObject() throws Exception {
        return this.cacheManager;
    }

    @Override
    public Class<?> getObjectType() {
        return CacheManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (this.cacheManager != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Shutting down EhCache Manager");
            }
            this.cacheManager.shutdown();
        }
    }

}
