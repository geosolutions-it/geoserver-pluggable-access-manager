package it.geosolutions.geoserver.security.impl;

import it.geosolutions.geoserver.security.AccessLimitsBuilder;
import it.geosolutions.geoserver.security.config.AccessLimitsBuilderConfiguration;
import it.geosolutions.geoserver.security.config.PluggableAccessManagerConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.Predicates;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.LayerGroupAccessLimits;
import org.geoserver.security.ResourceAccessManager;
import org.geoserver.security.StyleAccessLimits;
import org.geoserver.security.WorkspaceAccessLimits;
import org.geotools.util.logging.Logging;
import org.opengis.filter.Filter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;

/**
 * A {@link ResourceAccessManager} implementation that uses {@link AccessLimitsBuilder} beans to construct access limits to resource.
 * 
 * <p>
 * The association between resources and access limits builders is controlled by the provided {@link PluggableAccessManagerConfiguration}.
 * </p>
 * 
 * <p>
 * Implementation Notes:
 * <ul>
 * <li>Administrators can do anything; this behavior cannot be configured</li>
 * <li>Workspace access limits are static: admins can do anything, others can just read</li>
 * <li>Styles and Layer Groups are not restricted</li>
 * <ul>
 * </p>
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class PluggableAccessManager implements ResourceAccessManager, ApplicationContextAware {

    private static final Logger LOGGER = Logging.getLogger(PluggableAccessManager.class);

    /** The bean name of the access limits builder used to grant admin rights. */
    static final String ADMIN_ACCESS_LIMITS_BUILDER = "allowAllAccessLimitsBuilder";

    private PluggableAccessManagerConfiguration configuration;

    private ReentrantReadWriteLock configurationLock;

    private ApplicationContext context;

    /** Internal cache of access limits builder beans. */
    Map<String, AccessLimitsBuilder> accessLimitsBuildersCache;

    /** Number of bean lookups from Spring context - for testing purposes */
    int numLookups = 0;

    /**
     * Default constructor.
     */
    public PluggableAccessManager() {
        this.configurationLock = new ReentrantReadWriteLock();
        accessLimitsBuildersCache = new HashMap<String, AccessLimitsBuilder>();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * TODO: workspace access limits are static: admin can do anything, others can just read
     */
    @Override
    public WorkspaceAccessLimits getAccessLimits(Authentication user, WorkspaceInfo workspace) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Getting access limits for workspace {0}", workspace.getName());
        }
        PluggableAccessManagerConfiguration config = getConfiguration();
        if (isAdmin(user)) {
            LOGGER.log(Level.FINE, "Admin level access, returning "
                    + "full rights for workspace {0}", workspace.getName());
            return new WorkspaceAccessLimits(config.getCatalogMode(), true, true, true);
        }
        return new WorkspaceAccessLimits(config.getCatalogMode(), true, false, false);
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication user, LayerInfo layer) {
        return getAccessLimits(user, layer.getResource());
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication user, ResourceInfo resource) {
        AccessLimitsBuilder alb = getAccessLimitBuilder(user, resource);
        return alb.buildAccessLimits(user, resource, getConfiguration().getCatalogMode());
    }

    @Override
    public StyleAccessLimits getAccessLimits(Authentication user, StyleInfo style) {
        LOGGER.fine("No limits on styles");
        return null;
    }

    @Override
    public LayerGroupAccessLimits getAccessLimits(Authentication user, LayerGroupInfo layerGroup) {
        LOGGER.fine("No limits on layer groups");
        return null;
    }

    @Override
    public Filter getSecurityFilter(Authentication user, Class<? extends CatalogInfo> clazz) {
        // TODO: verify this is correct
        return Predicates.acceptAll();
    }

    /**
     * @see AccessLimitsBuilder#getDataSecurityToken(Authentication, ResourceInfo)
     * 
     * @param user the authenticated user
     * @param resource the resource to access
     * @return a unique token encoding the user's access level to the resource
     */
    public String getDataSecurityToken(Authentication user, ResourceInfo resource) {
        AccessLimitsBuilder alb = getAccessLimitBuilder(user, resource);
        return alb.getDataSecurityToken(user, resource);
    }

    /**
     * @return the access manager configuration
     */
    public PluggableAccessManagerConfiguration getConfiguration() {
        configurationLock.readLock().lock();
        try {
            return configuration;
        } finally {
            configurationLock.readLock().unlock();
        }
    }

    /**
     * @param configuration the access manager configuration to set
     */
    public void setConfiguration(PluggableAccessManagerConfiguration configuration) {
        configurationLock.writeLock().lock();
        try {
            this.configuration = configuration;
        } finally {
            configurationLock.writeLock().unlock();
        }
    }

    boolean isAdmin(Authentication user) {
        return GeoServerExtensions.bean(GeoServerSecurityManager.class)
                .checkAuthenticationForAdminRole(user);
    }

    AccessLimitsBuilder getAccessLimitBuilder(Authentication user, ResourceInfo resource) {
        String workspace = resource.getStore().getWorkspace().getName();
        String name = resource.getName();
        PluggableAccessManagerConfiguration config = getConfiguration();

        Map<String, Object> options = Collections.emptyMap();
        String accessLimitsBuilderBeanName = null;
        if (isAdmin(user)) {
            accessLimitsBuilderBeanName = ADMIN_ACCESS_LIMITS_BUILDER;
        } else {
            // lookup configuration first
            AccessLimitsBuilderConfiguration limitsBuilderConf = config
                    .getLimitsBuilderConfiguration(workspace, name);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Access limits will be calculated by this builder: "
                        + limitsBuilderConf.getBeanName());
            }
            // then retrieve bean name and options from config
            accessLimitsBuilderBeanName = limitsBuilderConf.getBeanName();
            options = limitsBuilderConf.getOptions();
        }

        AccessLimitsBuilder alb = null;
        try {
            alb = getAccessLimitBuilder(accessLimitsBuilderBeanName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to lookup access limits builder: "
                    + accessLimitsBuilderBeanName, e);
        }
        if (alb != null) {
            // set configuration options
            alb.setOptions(options);
        }
        return alb;
    }

    /**
     * Looks up an access limits builder bean from the Spring context by its name.
     * 
     * <p>
     * If a bean is already present in the internal cache, no lookup is performed.
     * </p>
     * 
     * @param beanName the bean name
     */
    AccessLimitsBuilder getAccessLimitBuilder(String beanName) {
        if (!accessLimitsBuildersCache.containsKey(beanName)) {
            AccessLimitsBuilder builder = (AccessLimitsBuilder) context.getBean(beanName);
            accessLimitsBuildersCache.put(beanName, builder);
            numLookups++;
        }
        return accessLimitsBuildersCache.get(beanName);
    }
}
