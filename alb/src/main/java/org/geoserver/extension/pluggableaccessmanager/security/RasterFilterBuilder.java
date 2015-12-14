package org.geoserver.extension.pluggableaccessmanager.security;

import java.io.IOException;

import org.geoserver.catalog.ResourceInfo;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.security.core.Authentication;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Interface for raster filter builders.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public interface RasterFilterBuilder {

    /**
     * Constructs a MultiPolygon geometry that represents the accessible area of the specified resource.
     * 
     * <p>
     * The resulting MultiPolygon is built by combining all the Polygon and MultiPolygon geometries of the selected features in the specified filter layer.
     * </p>
     * 
     * @param user the user
     * @param resource the resource
     * @param filterLayerName the layer containing the geometries used to construct the filter 
     * @param filterLayerFilter only the features matching this filter will be used to construct the raster filter
     * @return a MultiPolygon representing the accessible area
     * @throws IOException
     */
    public MultiPolygon buildRasterFilter(Authentication user, ResourceInfo resource,
            Name filterLayerName, Filter filterLayerFilter) throws IOException;

}
