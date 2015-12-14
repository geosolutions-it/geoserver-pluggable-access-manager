package org.geoserver.extension.pluggableaccessmanager.data.impl;

import static org.geoserver.security.KeyAuthenticationToken.DEFAULT_URL_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.geoserver.extension.pluggableaccessmanager.data.impl.CachingDataAccessAdapter;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.Request;
import org.geoserver.security.KeyAuthenticationToken;
import org.geoserver.security.impl.GeoServerUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/testApplicationContext.xml")
public class CachingDataAccessAdapterTest {

    private static final UserDetails USER_BOB = new GeoServerUser("bob");

    private static final UserDetails USER_ALICE = new GeoServerUser("alice");

    private static final String PERMISSIONS_BOB = "105,106";

    private static final String PERMISSIONS_ALICE = "105";

    @Autowired
    private ApplicationContext applicationContext;

    private CachingDataAccessAdapter cachingDataAccess;

    @Before
    public void beforeTest() throws Exception {
        cachingDataAccess = applicationContext.getBean(CachingDataAccessAdapter.class);
        assertNotNull(cachingDataAccess);
        cachingDataAccess.resetCachedMethodCallsCount();
        cachingDataAccess.clearAllCacheEntries();
        Dispatcher.REQUEST.set(new Request());
        Dispatcher.REQUEST.get().setKvp(new HashMap<Object, Object>());
    }

    private Authentication loginBob(String token) {
        Dispatcher.REQUEST.get().getKvp().put(DEFAULT_URL_PARAM, token);
        return new KeyAuthenticationToken(token, DEFAULT_URL_PARAM, USER_BOB);
    }

    private Authentication loginAlice(String token) {
        Dispatcher.REQUEST.get().getKvp().put(DEFAULT_URL_PARAM, token);
        return new KeyAuthenticationToken(token, DEFAULT_URL_PARAM, USER_ALICE);
    }

    @Test
    public void testUserPermissionsCaching() throws IOException {
        // check that cache is empty
        assertEquals(0, cachingDataAccess.getCountCachedMethodCalls());

        // login bob
        Authentication bob = loginBob("session-bob");
        List<String> permissions = cachingDataAccess.getUserPermissions(bob);
        checkPermissions(PERMISSIONS_BOB, permissions);
        // check that actual call to delegate method was executed
        assertEquals(1, cachingDataAccess.getCountCachedMethodCalls());
        // do the same call - this time should hit the cache
        permissions = cachingDataAccess.getUserPermissions(bob);
        checkPermissions(PERMISSIONS_BOB, permissions);
        assertEquals(1, cachingDataAccess.getCountCachedMethodCalls());

        // login alice
        Authentication alice = loginAlice("session-alice");
        // ask permissions for another user --> cache miss
        permissions = cachingDataAccess.getUserPermissions(alice);
        checkPermissions(PERMISSIONS_ALICE, permissions);
        assertEquals(2, cachingDataAccess.getCountCachedMethodCalls());
    }

    @Test
    public void testTwoSessions() throws IOException {
        // check that cache is empty
        assertEquals(0, cachingDataAccess.getCountCachedMethodCalls());

        // login bob, the first time
        Authentication bobFirst = loginBob("session-bob-1");
        List<String> permissions = cachingDataAccess.getUserPermissions(bobFirst);
        checkPermissions(PERMISSIONS_BOB, permissions);
        // check that actual call to delegate method was executed
        assertEquals(1, cachingDataAccess.getCountCachedMethodCalls());

        // login bob, the second time
        Authentication bobSecond = loginBob("session-bob-2");
        // ask permissions for user bob --> cache miss
        permissions = cachingDataAccess.getUserPermissions(bobSecond);
        checkPermissions(PERMISSIONS_BOB, permissions);
        assertEquals(2, cachingDataAccess.getCountCachedMethodCalls());
    }

    private void checkPermissions(String expected, List<String> permissions) {
        assertNotNull(permissions);
        assertEquals(expected, StringUtils.join(permissions, ","));
    }

}
