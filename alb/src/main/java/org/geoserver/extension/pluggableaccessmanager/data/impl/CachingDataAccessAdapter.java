package org.geoserver.extension.pluggableaccessmanager.data.impl;

import static org.geoserver.extension.pluggableaccessmanager.ehcache.Defaults.PERMISSIONS_CACHE;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.geoserver.extension.pluggableaccessmanager.data.DataAccess;
import org.geoserver.extension.pluggableaccessmanager.data.DataAccessProvider;
import org.geoserver.extension.pluggableaccessmanager.data.EvictableCache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;

/**
 * Adapts a {@link DataAccess} to make its methods take an {@link Authentication} instance instead of a simple {@link String} where a user is needed;
 * this enables the caching of method results on a per user session basis.</p>
 * 
 * <p>
 * The cache configuration is annotation-driven and requires the class be instantiated as a Spring bean to function.
 * </p>
 * 
 * @author Stefano Costa, GeoSolutions
 */
public class CachingDataAccessAdapter implements EvictableCache {

    int countCachedMethodCalls = 0;

    /** The factory used to obtain the data access instance. */
    DataAccessProvider provider;

    /**
     * @return the provider
     */
    public DataAccessProvider getProvider() {
        return provider;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(DataAccessProvider provider) {
        this.provider = provider;
    }

    /**
     * Retrieves user permission from the database and caches the result.
     * 
     * @see DataAccess#getUserPermissions(String)
     * 
     * @param user the user
     * @return the user permissions
     * @throws IOException
     */
    @Cacheable(value = PERMISSIONS_CACHE, key = "T(org.geoserver.extension.pluggableaccessmanager.security.impl.CacheHelper).getCacheKey(#user)")
    public List<String> getUserPermissions(Authentication user) throws IOException {
        if (user == null) {
            return Collections.emptyList();
        }

        checkDataAccessProvider();

        this.countCachedMethodCalls++;
        return provider.getDataAccess().getUserPermissions(user.getName());
    }

    public void dispose() throws IOException {
        checkDataAccessProvider();

        provider.getDataAccess().dispose();
    }

    /**
     * Get the number of times a chached method is actually executed; useful to test that Spring's cache abstraction is doing its job properly.
     * 
     * @return the number of method calls
     */
    public int getCountCachedMethodCalls() {
        return this.countCachedMethodCalls;
    }

    /**
     * Resets to 0 the number of times a chached method is actually executed; useful to test that Spring's cache abstraction is doing its job
     * properly.
     */
    public void resetCachedMethodCallsCount() {
        this.countCachedMethodCalls = 0;
    }

    private void checkDataAccessProvider() {
        if (provider == null) {
            throw new IllegalStateException("Data access provider has not been set");
        }
    }

    @Override
    @CacheEvict(value = PERMISSIONS_CACHE, key="#key")
    public boolean clearCacheEntry(String key) {
        // no-op, just a placeholder to trigger cache eviction
        return true;
    }

    @Override
    @CacheEvict(value = PERMISSIONS_CACHE, allEntries=true)
    public boolean clearAllCacheEntries() {
        // no-op, just a placeholder to trigger cache eviction
        return true;
    }

}
