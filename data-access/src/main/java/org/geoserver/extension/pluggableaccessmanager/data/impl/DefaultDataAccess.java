package org.geoserver.extension.pluggableaccessmanager.data.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.extension.pluggableaccessmanager.data.DataAccess;
import org.geotools.data.Transaction;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.logging.Logging;

/**
 * {@link DataAccess} implementation wrapping a {@link JDBCDataStore}.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class DefaultDataAccess implements DataAccess {

    private static final Logger LOGGER = Logging.getLogger(DefaultDataAccess.class);

    /** The stored procedure call to retrieve user modules from the database. */
    private static final String GET_USER_ACCESS = "SELECT \"SERVICE_ID\" FROM service_access WHERE \"USER\" = ?";

    /** The wrapped {@link JDBCDataStore} instance. */
    JDBCDataStore delegate;

    /**
     * Constructor.
     * 
     * @param dataStore the {@link JDBCDataStore} to wrap (must not be <code>null</code>)
     */
    DefaultDataAccess(JDBCDataStore dataStore) {
        if (dataStore == null) {
            throw new IllegalArgumentException("Provided dataStore cannot be null");
        }
        this.delegate = dataStore;
    }

    @Override
    public void dispose() {
        if (delegate != null) {
            delegate.dispose();
        }
    }

    @Override
    public List<String> getUserPermissions(String login) throws IOException {
        ensureNotNull("login", login);

        try (Connection conn = delegate.getConnection(Transaction.AUTO_COMMIT);
                PreparedStatement ps = conn.prepareStatement(GET_USER_ACCESS)) {
            ps.setString(1, login);

            ps.execute();

            List<String> result = new ArrayList<String>();
            try (ResultSet rs = ps.getResultSet()) {
                while(rs.next()) {
                    result.add("'" + rs.getString(1) + "'");
                }
            }
            if (!result.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Retrieved permissions for user {0}: {1}",
                            new Object[] { login, result });
                }
                return result;
            } else {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "No permissions found for user {0}",
                            new Object[] { login });
                }
                return Collections.emptyList();
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private void ensureNotNull(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

}
