package org.geoserver.extension.pluggableaccessmanager.data.impl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.geoserver.extension.pluggableaccessmanager.data.DataAccess;
import org.geoserver.extension.pluggableaccessmanager.data.DataAccessProvider;
import org.geoserver.extension.pluggableaccessmanager.data.OnlineTestCase;
import org.geoserver.extension.pluggableaccessmanager.data.impl.DefaultDataAccessProvider;
import org.junit.Test;

public class DefaultDataAccessProviderTest extends OnlineTestCase {

    @Override
    protected boolean isOnline() throws Exception {
        return true;
    }

    @Override
    protected String getFixtureId() {
        return "data-access";
    }

    @Test
    public void testDataAccessCreation() throws IOException {
        DataAccessProvider provider = new DefaultDataAccessProvider(getFixtureBase(),
                getFixtureId() + ".properties");
        assertNotNull(provider);

        DataAccess dataAccess = provider.getDataAccess();
        assertNotNull(dataAccess);
    }
}
