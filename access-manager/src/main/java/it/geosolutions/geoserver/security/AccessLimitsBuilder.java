package it.geosolutions.geoserver.security;

import java.util.Map;

import org.geoserver.catalog.ResourceInfo;
import org.geoserver.security.CatalogMode;
import org.geoserver.security.DataAccessLimits;
import org.springframework.security.core.Authentication;

/**
 * Interface for a dynamic access limits builder.
 * 
 * TODO: add methods to build workspace access limits.
 * 
 * @author Lorenzo Natali, GeoSolutions
 */
public interface AccessLimitsBuilder {

    /**
     * Given the currently authenticated user, build access limits for the specified resource.
     * 
     * @param user the user
     * @param resource the resource to access
     * @param catalogMode the catalog mode
     * @return the DataAccessLimits to use
     */
    public DataAccessLimits buildAccessLimits(Authentication user, ResourceInfo resource,
            CatalogMode catalogMode);

    /**
     * Return a string token uniquely identifying a user's rights on the specified resource.
     * 
     * <p>The token can be used to configure the integrated GeoWebCache service so as to generate
     * user specific tile caches.</p>
     * 
     * @param user the authenticated user
     * @param resource the resource to access
     * @return a unique token encoding the user's access level to the resource
     */
    public String getDataSecurityToken(Authentication user, ResourceInfo resource);

    /**
     * Returns the generic configuration options that the access limit builder will interpret.
     * 
     * @return a copy of the internal options map
     */
    public Map<String, Object> getOptions();

    /**
     * Sets the generic configuration options that the access limit builder will interpret.
     * 
     * @param options the configuration options to set
     */
    public void setOptions(Map<String, Object> options);
}
