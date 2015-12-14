package org.geoserver.extension.pluggableaccessmanager.data.impl;

import java.io.IOException;

import org.geoserver.extension.pluggableaccessmanager.data.DataAccess;
import org.geoserver.extension.pluggableaccessmanager.data.DataAccessProvider;

/**
 * Mock {@link DataAccessProvider} implementation, used for testing.
 * 
 * <p>Returns instances of {@link MockDataAccess}.</p>
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class MockDataAccessProvider implements DataAccessProvider {

    @Override
    public DataAccess getDataAccess() throws IOException {
        return new MockDataAccess();
    }

}
