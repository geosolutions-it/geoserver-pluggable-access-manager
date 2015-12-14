package org.geoserver.extension.pluggableaccessmanager.security.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.security.KeyAuthenticationToken;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;

/**
 * Utility class that infers the cache key for the authenticated user.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class CacheHelper {

    private static final Logger LOGGER = Logging.getLogger(CacheHelper.class);

    public static final String NO_KEY = "unknown";

    /**
     * If key authentication is being used (see authkey community module), the auth key parameter value is extracted from the request and returned;
     * otherwise, the username is returned.
     * 
     * @param user the authenticated user
     * @return the cache key for the specified user
     */
    public static String getCacheKey(Authentication user) {
        String key = null;
        if (user instanceof KeyAuthenticationToken) {
            // fetch authentication token from request
            key = (String) user.getCredentials();
        }
        // fallback to user name
        if (key == null && user != null) {
            key = user.getName();
        }
        // if key is still null, set it to NO_KEY constant
        if (key == null) {
            key = NO_KEY;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Using cache key: {0}", key);
        }
        return key;
    }

}
