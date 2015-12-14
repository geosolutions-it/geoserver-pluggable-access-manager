package org.geoserver.extension.pluggableaccessmanager.data;

/**
 * Interface for caches whose entries can be programmatically cleared.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public interface EvictableCache {

    /**
     * Removes from the cache only the entry associated to the specified key. 
     * 
     * @param key the key of the entry that should be cleared
     * @return {@code true} if the entry was successfully removed, {@code false} otherwise
     */
    public boolean clearCacheEntry(String key);

    /**
     * Removes all cache entries.
     * 
     * @return {@code true} if the cache was successfully cleared, {@code false} otherwise
     */
    boolean clearAllCacheEntries();

}
