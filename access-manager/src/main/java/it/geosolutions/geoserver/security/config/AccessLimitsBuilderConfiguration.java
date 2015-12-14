package it.geosolutions.geoserver.security.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

/**
 * Plain Old Java Object (POJO) representing an access limits builder configuration.
 * 
 * <p>Can be serialized / deserialized using {@link XStream}.</p>
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class AccessLimitsBuilderConfiguration {

    private String id;
    private String beanName;
    private Map<String,Object> options;

    /**
     * @return the configuration id (should be unique)
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the configuration id to set (should be unique)
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the name of the access limits builder's bean in the Spring context
     */
    public String getBeanName() {
        return beanName;
    }
    /**
     * Sets the name of the access limits builder's bean in the Spring context.
     * 
     * @param beanName the bean name to set
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
    /**
     * Returns the options for this access limits builder.
     * 
     * <p>Note that this method never returns <code>null</code>: an empty
     * map is returned if no options are available.</p>
     * 
     * @return the configured options as a {@link Map}
     */
    public Map<String, Object> getOptions() {
        if (this.options == null) {
            this.options = new HashMap<String, Object>();
        }
        return options;
    }
    /**
     * @param options the options to set
     */
    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    /**
     * A configuration is valid if both {@code id} and {@code beanName} have both been specified.
     * 
     * @return {@code true} if the configuration is valid, {@code false} otherwise
     */
    public boolean isValid() {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        if (StringUtils.isBlank(beanName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        AccessLimitsBuilderConfiguration other = (AccessLimitsBuilderConfiguration) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AccessLimitsBuilderConfiguration [id=" + id + ", beanName=" + beanName
                + ", options=" + options + "]";
    }

}
