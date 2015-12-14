package org.geoserver.extension.pluggableaccessmanager.data;

import java.io.IOException;

/**
 * Simple interface for {@link DataAccess} factories.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public interface DataAccessProvider {

    /**
     * Returns a {@link DataAccess} implementation (which one depends on the provider implementation).
     * 
     * <p>
     * In general, it is advisable that implementations create just one instance of a data access.
     * </p>
     * 
     * @return a {@link DataAccess} instance
     * @throws IOException
     */
    public DataAccess getDataAccess() throws IOException;

}
