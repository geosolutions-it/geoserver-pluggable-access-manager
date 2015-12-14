package org.geoserver.extension.pluggableaccessmanager.data.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geoserver.extension.pluggableaccessmanager.data.DataAccess;

/**
 * Mock {@link DataAccess} implementation, used for testing.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class MockDataAccess implements DataAccess {

    @Override
    public List<String> getUserPermissions(String login) throws IOException {
        if ("bob".equals(login)) {
            return Arrays.asList("105", "106");
        } else if ("alice".equals(login)) {
            return Arrays.asList("105");
        } else if ("sam".equals(login)) {
            return Arrays.asList("110");
        }
        return Collections.emptyList();
    }

    @Override
    public void dispose() {
        // nothing to do, it's a mock after all
    }

}
