package org.geoserver.extension.pluggableaccessmanager.data;

import java.io.IOException;
import java.util.List;

/**
 * Provides a database abstraction interface.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public interface DataAccess {

    /**
     * Given a user name, retrieves a list of tokens identifying which permissions the specified user has been granted.
     * 
     * @param login the user name
     * @return a list of tokens conveying the information on which permissions have been granted
     * @throws IOException
     */
    public List<String> getUserPermissions(String login) throws IOException;

    /**
     * Disposes of the data access.
     */
    public void dispose();

}
