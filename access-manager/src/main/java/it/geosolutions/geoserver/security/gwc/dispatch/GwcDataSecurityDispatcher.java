package it.geosolutions.geoserver.security.gwc.dispatch;

import it.geosolutions.geoserver.security.impl.PluggableAccessManager;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.ows.AbstractDispatcherCallback;
import org.geoserver.ows.Request;
import org.geoserver.platform.Operation;
import org.geoserver.security.SecureCatalogImpl;
import org.geoserver.wms.GetMapRequest;
import org.geoserver.wms.MapLayerInfo;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This dispatcher enables GeoWebCache data security via the {@link PluggableAccessManager}.
 * 
 * <p>
 * Tiled WMS GetMap requests (i.e. with request parameter {@code TILED=true}) are modified by adding a special parameter,
 * {@value #GWC_DATA_SECURITY_PARAM}, which uniquely identifies the authenticated user's access rights to the requested resource.
 * </p>
 * 
 * <p>
 * The parameter value is obtained via a call to {@link PluggableAccessManager#getDataSecurityToken(Authentication, ResourceInfo)}.
 * </p>
 * 
 * <p>
 * A current limitation is that a {@value #GWC_DATA_SECURITY_PARAM} parameter filter must be manually setup for each secured layer in the tile layer
 * configuration page.
 * </p>
 * 
 * <p>The dispatcher can be disabled by calling {@code setGwcDataSecurityEnabled(false)}.</p>
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class GwcDataSecurityDispatcher extends AbstractDispatcherCallback {

    public static final String GWC_DATA_SECURITY_PARAM = "DATA_SECURITY";

    private static final Logger LOGGER = Logging.getLogger(GwcDataSecurityDispatcher.class);

    boolean gwcDataSecurityEnabled;

    SecureCatalogImpl secureCatalog;

    PluggableAccessManager pluggableAccessManager;

    /**
     * Retrieves a security token from the provided {@link PluggableAccessManager} instance and adds it as a parameter to the request.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Operation operationDispatched(Request request, Operation operation) {
        if (isCachable(request, operation) && isGwcDataSecurityEnabled()
                && pluggableAccessManager != null) {
            GetMapRequest getMap = (GetMapRequest) operation.getParameters()[0];
            LayerInfo layer = getLayerInfo(getMap);
            if (layer != null) {
                String securityToken = pluggableAccessManager.getDataSecurityToken(
                        getLoggedInUser(), layer.getResource());
                if (securityToken != null) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("Adding " + GWC_DATA_SECURITY_PARAM
                                + " to the request with value " + securityToken);
                    }
                    request.getRawKvp().put(GWC_DATA_SECURITY_PARAM, securityToken);
                }
            }
        }
        return operation;
    }

    /**
     * Cache only WMS GetMap requests, with {@code TILED} parameter set to {@code true}.
     * 
     * @param request the request
     * @param operation the operation
     * @return {@code true} if the request is cachable, {@code false} otherwise
     */
    boolean isCachable(Request request, Operation operation) {
        Map<?, ?> rawKvp = request.getRawKvp();
        if (rawKvp != null && "true".equalsIgnoreCase((String) rawKvp.get("TILED"))
                && "WMS".equalsIgnoreCase(operation.getService().getId())
                && "GetMap".equalsIgnoreCase(operation.getId())) {
            return true;
        }
        return false;
    }

    Authentication getLoggedInUser() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Returns a {@link LayerInfo} object describing the requested layer.
     * 
     * <p>
     * {@code null} is returned for multi-layer requests, as GeoWebCache is not caching them.
     * </p>
     * 
     * @param getMap the GetMap request
     * @return the layer info, or {@code null} if the request involved multiple layers
     */
    LayerInfo getLayerInfo(GetMapRequest getMap) {
        List<MapLayerInfo> mapLayers = getMap.getLayers();
        // don't consider
        if (mapLayers.size() == 1) {
            return mapLayers.get(0).getLayerInfo();
        }
        return null;
    }

    /**
     * @return {@code true} if the dispatcher is enabled, {@code false} otherwise
     */
    public boolean isGwcDataSecurityEnabled() {
        return gwcDataSecurityEnabled;
    }

    /**
     * @param gwcDataSecurityEnabled {@code true} to enable the dispatcher, {@code false} to disable it
     */
    public void setGwcDataSecurityEnabled(boolean gwcDataSecurityEnabled) {
        this.gwcDataSecurityEnabled = gwcDataSecurityEnabled;
    }

    /**
     * Enables setter injection of a {@link PluggableAccessManager} instance.
     * 
     * @param pluggableAccessManager the pluggable access manager to set 
     */
    public void setPluggableAccessManager(PluggableAccessManager pluggableAccessManager) {
        this.pluggableAccessManager = pluggableAccessManager;
    }

    /**
     * Enables setter injection of a {@link SecureCatalogImpl} instance.
     * 
     * @param secureCatalog the secure catalog to set
     */
    public void setSecureCatalog(SecureCatalogImpl secureCatalog) {
        this.secureCatalog = secureCatalog;
    }

}
