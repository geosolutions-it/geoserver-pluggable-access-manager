package org.geoserver.extension.pluggableaccessmanager.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geoserver.extension.pluggableaccessmanager.ehcache.Defaults;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/org/geoserver/extension/pluggableaccessmanager/cache/testContext.xml")
public class EhCacheManagerFactoryBeanTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testEhCacheManagerCreation() {
        CacheManager cacheManager = applicationContext.getBean(CacheManager.class);
        assertNotNull(cacheManager);

        assertEquals(2, cacheManager.getCacheNames().size());
        List<String> cacheNames = new ArrayList<String>(cacheManager.getCacheNames());
        Collections.sort(cacheNames);
        assertEquals(Defaults.PERMISSIONS_CACHE, cacheNames.get(0));
        assertEquals(Defaults.RASTER_FILTERS_CACHE, cacheNames.get(1));
    }

}
