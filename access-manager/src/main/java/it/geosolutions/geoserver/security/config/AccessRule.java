package it.geosolutions.geoserver.security.config;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.geotools.util.logging.Logging;

import com.thoughtworks.xstream.XStream;

/**
 * Plain Old Java Object (POJO) representing an access rule configuration.
 * 
 * <p>
 * Can be serialized / deserialized using {@link XStream}.
 * </p>
 * 
 * <p>
 * A rule is made up of three bits of information, which are all required:
 * <ul>
 *   <li>a <strong>workspace name</strong> (may be * to match any workspace)</li>
 *   <li>a <strong>layer name</strong> (may be * to match any layer)</li>
 *   <li>an access limits builder <strong>configuration id</strong></li>
 * </ul>
 * </p>
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class AccessRule {

    private static final Logger LOGGER = Logging.getLogger(AccessRule.class);

    /** Wildcard to match any object. */
    public static final String ANY = "*";

    private String workspace;

    private String layer;

    private String accessLimitsBuilder;

    /**
     * @return the workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @param workspace the workspace to set
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * @return the layer
     */
    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    /**
     * @return the access limits builder configuration id
     */
    public String getAccessLimitsBuilder() {
        return accessLimitsBuilder;
    }

    /**
     * @param accessLimitsBuilder the access limits builder configuration id to set
     */
    public void setAccessLimitsBuilder(String accessLimitsBuilder) {
        this.accessLimitsBuilder = accessLimitsBuilder;
    }

    /**
     * An access rule is valid if {@code workspace}, {@code layer} and {@code accessLimitsBuilder} have all been specified.
     * 
     * @return {@code true} if the rule is valid, {@code false} otherwise
     */
    public boolean isValid() {
        if (StringUtils.isBlank(workspace)) {
            return false;
        }
        if (StringUtils.isBlank(layer)) {
            return false;
        }
        if (StringUtils.isBlank(accessLimitsBuilder)) {
            return false;
        }
        return true;
    }

    /**
     * Generates this rule's key by concatenating the {@code workspace} and {@code layer} properties.
     * 
     * <p>Note that the rule key should be unique.</p>
     * 
     * @return the rule key, or {@code null} if the rule is invalid
     */
    public String getKey() {
        if (!isValid()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Rule is not valid, cannot generate key");
            }
            return null;
        }
        return buildKey(workspace, layer);
    }

    /**
     * Utility method to generate a rule key.
     * 
     * @param workspace the workspace
     * @param layer the layer
     * @return a rule key, i.e. &lt;workspace&gt;.&lt;layer&gt;
     */
    public static String buildKey(String workspace, String layer) {
        return workspace + "." + layer;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((layer == null) ? 0 : layer.hashCode());
        result = prime * result + ((workspace == null) ? 0 : workspace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AccessRule other = (AccessRule) obj;
        if (layer == null) {
            if (other.layer != null)
                return false;
        } else if (!layer.equals(other.layer))
            return false;
        if (workspace == null) {
            if (other.workspace != null)
                return false;
        } else if (!workspace.equals(other.workspace))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AccessRule [workspace=" + workspace + ", layer=" + layer + ", accessLimitsBuilder="
                + accessLimitsBuilder + "]";
    }

}
