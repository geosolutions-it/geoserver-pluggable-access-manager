package it.geosolutions.geoserver.security.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.geoserver.security.CatalogMode;
import org.geotools.util.logging.Logging;

import com.thoughtworks.xstream.XStream;

/**
 * Plain Old Java Object (POJO) representing an pluggable access manager configuration.
 * 
 * <p>
 * Can be serialized / deserialized using {@link XStream}.
 * </p>
 * 
 * <p>
 * The default configuration allows any user to access any resource. The default catalog mode is {@link CatalogMode#HIDE}. 
 * </p>
 * 
 * <p>
 * Note that this class keeps internal state to speed up configuration lookups and is thus eminently NOT thread-safe.
 * </p>
 * 
 * @author Stefano Costa, GeoSolutions
 * 
 */
public class PluggableAccessManagerConfiguration {

    private static final Logger LOGGER = Logging
            .getLogger(PluggableAccessManagerConfiguration.class);

    /** Default id for The default access lmits builder configuration. */
    static final String DEFAULT_CONFIG_ID = "default";

    /** Default bean name for the default access lmits builder. */
    static final String DEFAULT_CONFIG_BEAN_NAME = "allowAllAccessLimitsBuilder";

    /**
     * Default catalog mode.
     * 
     * @see CatalogMode#HIDE
     */
    static final CatalogMode DEFAULT_CATALOG_MODE = CatalogMode.HIDE;

    private List<AccessLimitsBuilderConfiguration> accessLimitsBuilders;

    private List<AccessRule> rules;

    /** {@link HashMap} to easily lookup access limits builder configurations by id. */
    private HashMap<String, AccessLimitsBuilderConfiguration> buildersById;

    /** {@link HashMap} to easily lookup access rules by key. */
    private HashMap<String, AccessRule> rulesByKey;

    /** Default access limits builder configuration. Applied when no rule matches. */
    private AccessLimitsBuilderConfiguration defaultAccessLimitsBuilder;

    private CatalogMode catalogMode;

    /**
     * Note that this method never returns <code>null</code>, but an empty list is returned if no access limits builders have been configured.
     * 
     * @return the access limits builder configurations
     */
    public List<AccessLimitsBuilderConfiguration> getAccessLimitsBuilders() {
        if (this.accessLimitsBuilders == null) {
            this.accessLimitsBuilders = new ArrayList<AccessLimitsBuilderConfiguration>();
        }
        return accessLimitsBuilders;
    }

    /**
     * Sets the access limits builder configurations and updates the internal state.
     * 
     * @param accessLimitsBuilders the access limits builder configurations to set
     */
    public void setAccessLimitsBuilders(List<AccessLimitsBuilderConfiguration> accessLimitsBuilders) {
        setAccessLimitsBuilders(accessLimitsBuilders, true);
    }

    /**
     * Sets the access limits builder configurations.
     * 
     * <p>
     * If {@code updateState} is {@code true}, updates the internal state.
     * </p>
     * 
     * @param accessLimitsBuilders the access limits builder configurations to set
     * @param updateState if {@code true}, internal state is updated
     */
    public void setAccessLimitsBuilders(
            List<AccessLimitsBuilderConfiguration> accessLimitsBuilders, boolean updateState) {
        this.accessLimitsBuilders = accessLimitsBuilders;
        if (updateState) {
            updateState();
        }
    }

    /**
     * Note that this method never returns <code>null</code>, but an empty list is returned if no rules have been configured.
     * 
     * @return the access rules
     */
    public List<AccessRule> getRules() {
        if (this.rules == null) {
            this.rules = new ArrayList<AccessRule>();
        }
        return rules;
    }

    /**
     * Sets the access rules and updates the internal state.
     * 
     * @param rules the access rules to set
     */
    public void setRules(List<AccessRule> rules) {
        setRules(rules, true);
    }

    /**
     * Sets the access rules.
     * 
     * <p>
     * If {@code updateState} is {@code true}, updates the internal state.
     * </p>
     * 
     * @param rules the access rules to set
     * @param updateState if {@code true}, internal state is updated
     */
    public void setRules(List<AccessRule> rules, boolean updateState) {
        this.rules = rules;
        if (updateState) {
            updateState();
        }
    }

    /**
     * Note that this method never returns <code>null</code>, but a default access limits builder configuration is returned if none is available.
     * 
     * @return the default access limits builder configuration
     */
    public AccessLimitsBuilderConfiguration getDefaultAccessLimitsBuilder() {
        if (defaultAccessLimitsBuilder == null) {
            defaultAccessLimitsBuilder = new AccessLimitsBuilderConfiguration();
            defaultAccessLimitsBuilder.setId(DEFAULT_CONFIG_ID);
            defaultAccessLimitsBuilder.setBeanName(DEFAULT_CONFIG_BEAN_NAME);
            ;
        }
        return defaultAccessLimitsBuilder;
    }

    /**
     * @param defaultAccessLimitsBuilder the default access limits builder configuration to set
     */
    public void setDefaultAccessLimitsBuilder(
            AccessLimitsBuilderConfiguration defaultAccessLimitsBuilder) {
        this.defaultAccessLimitsBuilder = defaultAccessLimitsBuilder;
    }

    /**
     * Note that this method never returns <code>null</code>, but {@link #DEFAULT_CATALOG_MODE} is returned if a catalog mode has not been configured.
     * 
     * @return the catalog mode
     */
    public CatalogMode getCatalogMode() {
        if (catalogMode == null) {
            catalogMode = DEFAULT_CATALOG_MODE;
        }
        return catalogMode;
    }

    /**
     * @param catalogMode the catalog mode to set
     */
    public void setCatalogMode(CatalogMode catalogMode) {
        this.catalogMode = catalogMode;
    }

    /**
     * A configuration is valid if all access limits builder configurations and access rules are valid, and if all references from access rules to
     * builder configurations can be resolved.
     * 
     * @return {@code true} if the configuration is valid, {@code false} otherwise
     */
    public boolean isValid() {
        Set<String> builderIds = new HashSet<String>();
        for (AccessLimitsBuilderConfiguration builderConf : getAccessLimitsBuilders()) {
            if (!builderConf.isValid()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Invalid limits builder detected: {0}", builderConf);
                }
                return false;
            }
            builderIds.add(builderConf.getId());
        }
        for (AccessRule accessRule : getRules()) {
            if (!accessRule.isValid()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Invalid rule detected: {0}", accessRule);
                }
                return false;
            }
            if (!builderIds.contains(accessRule.getAccessLimitsBuilder())) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(
                            Level.FINE,
                            "Invalid configuration detected: rule references unknown access limits builder {0}",
                            accessRule.getAccessLimitsBuilder());
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Updates internal state, i.e. the hash maps used to speed up lookups.
     * 
     * <p>
     * If the configuration is invalid, the state is not updated.
     * </p>
     */
    void updateState() {
        if (!isValid()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Configuration is invalid, cannot rebuild internal state");
            }
            return;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Rebuilding configuration internal state");
        }

        Map<String, AccessLimitsBuilderConfiguration> buildersById = getBuildersById();
        buildersById.clear();
        Map<String, AccessRule> rulesByKey = getRulesByKey();
        rulesByKey.clear();

        List<AccessLimitsBuilderConfiguration> builders = getAccessLimitsBuilders();
        for (AccessLimitsBuilderConfiguration builder : builders) {
            buildersById.put(builder.getId(), builder);
        }
        List<AccessRule> rules = getRules();
        for (AccessRule rule : rules) {
            rulesByKey.put(rule.getKey(), rule);
        }
    }

    /**
     * Finds the access limits builder configuration for the resource identified by the specified {@code workspace} and {@code layer}.
     * 
     * <p>
     * The rule matching algorithm looks up the best matching rule trying the following keys in sequence:
     * <ol>
     * <li>&lt;workspace&gt;.&lt;layer&gt;</li>
     * <li>&lt;workspace&gt;.*</li>
     * <li>*.&lt;layer&gt;</li>
     * <li>*.*</li>
     * </ol>
     * </p>
     * 
     * <p>
     * If a rule was found, the access limits builder configuration with the id specified in the rule is returned.
     * </p>
     * 
     * <p>
     * If no rule was found, the default access limits builder configuration is returned.
     * </p>
     * 
     * @param workspace the workspace
     * @param layer the layer (can be * to lookup configuration by workspace only)
     * @return the access limits builder restricting the resource
     */
    public AccessLimitsBuilderConfiguration getLimitsBuilderConfiguration(String workspace,
            String layer) {
        if (StringUtils.isBlank(workspace) || AccessRule.ANY.equals(workspace)) {
            throw new IllegalArgumentException("workspace must be specified");
        }
        if (StringUtils.isBlank(layer)) {
            throw new IllegalArgumentException("layer must be specified, use " + AccessRule.ANY
                    + " to lookup configuration by workspace only");
        }
        if (!isValid()) {
            throw new IllegalArgumentException(
                    "Current configuration is not valid, please fix before using it");
        }

        // TODO: add log statements
        Map<String, AccessRule> rulesByKey = getRulesByKey();
        Map<String, AccessLimitsBuilderConfiguration> buildersById = getBuildersById();
        AccessRule bestMatch = null;
        String mostSpecificKey = AccessRule.buildKey(workspace, layer);
        if (!AccessRule.ANY.equals(layer) && rulesByKey.containsKey(mostSpecificKey)) {
            bestMatch = rulesByKey.get(mostSpecificKey);
        } else {
            String workspaceOnlyKey = AccessRule.buildKey(workspace, AccessRule.ANY);
            if (rulesByKey.containsKey(workspaceOnlyKey)) {
                bestMatch = rulesByKey.get(workspaceOnlyKey);
            } else {
                String layerOnlyKey = AccessRule.buildKey(AccessRule.ANY, layer);
                if (!AccessRule.ANY.equals(layer) && rulesByKey.containsKey(layerOnlyKey)) {
                    bestMatch = rulesByKey.get(layerOnlyKey);
                } else {
                    String mostGenericKey = AccessRule.buildKey(AccessRule.ANY, AccessRule.ANY);
                    if (rulesByKey.containsKey(mostGenericKey)) {
                        bestMatch = rulesByKey.get(mostGenericKey);
                    }
                }
            }
        }

        if (bestMatch != null) {
            return buildersById.get(bestMatch.getAccessLimitsBuilder());
        } else {
            return getDefaultAccessLimitsBuilder();
        }
    }

    /**
     * @return the hash map storing access limits builder configurations by id. An empty map is returned instead of {@code null} if no configuration
     *         is available
     */
    Map<String, AccessLimitsBuilderConfiguration> getBuildersById() {
        if (this.buildersById == null) {
            this.buildersById = new HashMap<String, AccessLimitsBuilderConfiguration>();
        }
        return buildersById;
    }

    /**
     * @return the hash map storing rules by key. An empty map is returned instead of {@code null} if no rules are available
     */
    Map<String, AccessRule> getRulesByKey() {
        if (this.rulesByKey == null) {
            this.rulesByKey = new HashMap<String, AccessRule>();
        }
        return rulesByKey;
    }

    /**
     * Invoked by XStream after deserialization.
     * 
     * <p>
     * Takes care of initializing the internal state and ensuring that the default access limits builder configuration's id is set.
     * </p>
     * 
     * @return a fully initialized configuration object
     */
    private Object readResolve() {
        // make sure the id of the default access limits builder configuration is set
        if (defaultAccessLimitsBuilder != null
                && StringUtils.isEmpty(defaultAccessLimitsBuilder.getId())) {
            defaultAccessLimitsBuilder.setId(DEFAULT_CONFIG_ID);
        }
        updateState();
        return this;
    }

}
