package org.geoserver.extension.pluggableaccessmanager.security.impl;

import static org.geoserver.extension.pluggableaccessmanager.ehcache.Defaults.RASTER_FILTERS_CACHE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.extension.pluggableaccessmanager.data.EvictableCache;
import org.geoserver.extension.pluggableaccessmanager.security.RasterFilterBuilder;
import org.geotools.data.DataAccess;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implementation of {@link RasterFilterBuilder} that caches the calculated geometries.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class CachingRasterFilterBuilder implements RasterFilterBuilder, EvictableCache {

    private static final Logger LOGGER = Logging.getLogger(CachingRasterFilterBuilder.class);

    @Override
    @Cacheable(value = RASTER_FILTERS_CACHE, key = "T(org.geoserver.extension.pluggableaccessmanager.security.impl.CacheHelper).getCacheKey(#user)")
    public MultiPolygon buildRasterFilter(Authentication user, ResourceInfo resource,
            Name maskLayerName, Filter filter) throws IOException {
        return buildRasterFilterInternal(resource, maskLayerName, filter);
    }

    MultiPolygon buildRasterFilterInternal(ResourceInfo resource, Name maskLayerName, Filter filter)
            throws IOException {
        if (maskLayerName == null) {
            throw new IllegalArgumentException("The name of the mask layer must be set");
        }
        LayerInfo maskLayer = resource.getCatalog().getLayerByName(maskLayerName);
        if (maskLayer == null) {
            throw new IllegalArgumentException("Layer " + maskLayerName
                    + " not found in the catalog");
        }
        if (!(maskLayer.getResource() instanceof FeatureTypeInfo)) {
            throw new IllegalArgumentException(maskLayerName + " is not a vector layer");
        }
        if (filter == null) {
            filter = Filter.INCLUDE;
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Mask layer: {0}; Mask layer filter: {1}", new Object[] {
                    maskLayerName, filter });
        }
        FeatureTypeInfo maskFeatureType = (FeatureTypeInfo) maskLayer.getResource();
        DataAccess<? extends FeatureType, ? extends Feature> dataStore = maskFeatureType.getStore()
                .getDataStore(null);
        return getRoiArea(dataStore, new NameImpl(maskFeatureType.getNativeName()), filter);
    }

    MultiPolygon getRoiArea(DataAccess<? extends FeatureType, ? extends Feature> dataStore,
            Name typeName, Filter filter) throws IOException {
        SimpleFeatureSource source = (SimpleFeatureSource) dataStore.getFeatureSource(typeName);
        SimpleFeatureCollection features = source.getFeatures(filter);
        SimpleFeatureIterator iterator = features.features();
        MultiPolygon roiArea = null;
        List<Polygon> polygonParts = new ArrayList<Polygon>();
        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            // only Polygon and MultiPolygon geometries are taken into account
            if (geom instanceof Polygon) {
                polygonParts.add((Polygon) geom);
            } else if (geom instanceof MultiPolygon) {
                for (int i = 0; i < geom.getNumGeometries(); i++) {
                    polygonParts.add((Polygon) geom.getGeometryN(i));
                }
            }
        }
        if (polygonParts.size() > 0) {
            roiArea = new MultiPolygon(polygonParts.toArray(new Polygon[] {}), polygonParts.get(0)
                    .getFactory());
        }
        return roiArea;
    }

    @Override
    @CacheEvict(value = RASTER_FILTERS_CACHE, key = "#key")
    public boolean clearCacheEntry(String key) {
        // no-op, just a placeholder to trigger cache eviction
        return true;
    }

    @Override
    @CacheEvict(value = RASTER_FILTERS_CACHE, allEntries = true)
    public boolean clearAllCacheEntries() {
        // no-op, just a placeholder to trigger cache eviction
        return true;
    }

}
