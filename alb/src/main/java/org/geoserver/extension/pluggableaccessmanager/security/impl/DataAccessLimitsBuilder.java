package org.geoserver.extension.pluggableaccessmanager.security.impl;

import it.geosolutions.geoserver.security.AccessLimitsBuilder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.extension.pluggableaccessmanager.data.DataAccess;
import org.geoserver.extension.pluggableaccessmanager.data.impl.CachingDataAccessAdapter;
import org.geoserver.extension.pluggableaccessmanager.security.RasterFilterBuilder;
import org.geoserver.security.CatalogMode;
import org.geoserver.security.CoverageAccessLimits;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.VectorAccessLimits;
import org.geoserver.security.WMSAccessLimits;
import org.geotools.feature.NameImpl;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.security.core.Authentication;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * {@link AccessLimitsBuilder} implementation that uses a {@link DataAccess} to retrieve a list of user permission tokens and uses them to build a CQL
 * filter based on a configurable template.
 * 
 * @author Stefano Costa, GeoSolutions
 *
 */
public class DataAccessLimitsBuilder implements AccessLimitsBuilder {

    private static final Logger LOGGER = Logging.getLogger(DataAccessLimitsBuilder.class);

    /** The name of the option used to specify the CQL filter template. */
    static final String CQL_FILTER_TEMPLATE_OPTION = "cql_filter_template";

    /** The name of the option used to specify the layer to use for ROI based raster filters. */
    static final String RASTER_MASK_LAYER_OPTION = "raster_mask_layer";

    /**
     * The name of the option used to specify the CQL filter to apply on the raster mask layer.
     * 
     * <p>
     * If not specified, the default CQL filter applied on the data layer will be used.
     * </p>
     */
    static final String RASTER_MASK_FILTER_TEMPLATE_OPTION = "raster_mask_filter_template";

    /** The default CQL filter template. */
    static final String DEFAULT_CQL_FILTER_TEMPLATE = "service_id IN ({0})";

    /** Empty MultiPolygon serving as a deny-all raster filter. */
    static final MultiPolygon DENY_ALL_RASTER_FILTER;

    static {
        GeometryBuilder gb = new GeometryBuilder();
        DENY_ALL_RASTER_FILTER = gb.multiPolygon(gb.polygon());
    }

    /** The factory used to obtain the data access instance. */
    CachingDataAccessAdapter dataAccessAdapter;

    /** The bean used to build raster filters. */
    RasterFilterBuilder rasterFilterBuilder;

    /** Configuration options. */
    Map<String, Object> options;

    /**
     * Constructor.
     * 
     * @param dataAccessAdapter the data access factory
     * @throws IOException
     */
    public DataAccessLimitsBuilder(CachingDataAccessAdapter dataAccessAdapter,
            RasterFilterBuilder rasterFilterBuilder) throws IOException {
        this.dataAccessAdapter = dataAccessAdapter;
        this.rasterFilterBuilder = rasterFilterBuilder;
        this.options = Collections.emptyMap();
    }

    @Override
    public DataAccessLimits buildAccessLimits(Authentication user, ResourceInfo resource,
            CatalogMode catalogMode) {
        Filter accessFilter = getAccessFilter(user);
        return buildAccessLimitsInternal(user, resource, catalogMode, accessFilter);
    }

    /**
     * Invokes {@link DataAccess#getUserPermissions(String)} to retrieve a list of user permission tokens and uses them to construct a CQL filter,
     * based on the specified template.
     * 
     * <p>
     * The CQL filter defines both read and write limits.
     * </p>
     * 
     * <p>
     * If no template was specified in the {@code options}, the default one is used: {@value #DEFAULT_CQL_FILTER_TEMPLATE}.
     * </p>
     * 
     * <p>
     * If user permissions cannot be retrieved or the CQL filter cannot be constructed, access is denied.
     * </p>
     */
    Filter getAccessFilter(Authentication user) {
        List<String> permissions = null;
        try {
            permissions = getPermissions(user);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not retrieve user permissions: denying access to all",
                    e);
            return Filter.EXCLUDE;
        }

        String cqlFilterTemplate = getCqlFilterTemplate(options);
        Filter accessFilter = null;
        try {
            accessFilter = buildCqlFilter(cqlFilterTemplate, permissions);
        } catch (CQLException e) {
            LOGGER.log(Level.SEVERE, "Could not build CQL filter using template "
                    + cqlFilterTemplate + " and permissions " + permissions
                    + ": denying access to all");
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Error parsing CQL filter", e);
            }
            accessFilter = Filter.EXCLUDE;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Access filter for user {0}: {1}", new Object[] {
                    user.getName(), accessFilter });
        }
        return accessFilter;
    }

    /**
     * Builds a raster filter for the specified resource, i.e. a MultiPolygon delimiting the area the user <strong>can</strong> access.
     * 
     * <p>
     * If no mask layer was specified in the options, {@code null} is returned and access will not be restricted.
     * </p>
     * 
     * <p>
     * If the specified CQL filter matches no geometries or an error occurs, an empty MultiPolygon is returned and access is denied on the entire
     * extent.
     * </p>
     * 
     * @param user the user
     * @param resource the restricted resource
     * @return the geometry of the accessible area
     */
    MultiPolygon buildRasterFilter(Authentication user, ResourceInfo resource) {
        Name maskLayerName = getMaskLayerName(options);
        if (maskLayerName == null) {
            // returning null is equivalent to an allow-all filter
            return null;
        }

        List<String> permissions = null;
        try {
            permissions = getPermissions(user);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not retrieve user permissions: denying access to all",
                    e);
            return DENY_ALL_RASTER_FILTER;
        }

        String maksFilterTemplate = getMaskFilterTemplate(options);
        Filter maskFilter = null;
        try {
            maskFilter = buildCqlFilter(maksFilterTemplate, permissions);
        } catch (CQLException e) {
            LOGGER.log(Level.SEVERE, "Could not build CQL filter using template "
                    + maksFilterTemplate + " and permissions " + permissions
                    + ": denying access to all");
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Error parsing CQL filter", e);
            }
            maskFilter = Filter.EXCLUDE;
        }

        MultiPolygon roiArea = null;
        try {
            roiArea = rasterFilterBuilder.buildRasterFilter(user, resource, maskLayerName,
                    maskFilter);
            if (roiArea == null) {
                roiArea = DENY_ALL_RASTER_FILTER;
            } else {
                // reproject area if necessary
                LayerInfo mask = resource.getCatalog().getLayerByName(maskLayerName);
                roiArea = reprojectRoiArea(mask.getResource().getCRS(), resource.getCRS(), roiArea);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not retrieve ROI area: denying access to all", e);
            return DENY_ALL_RASTER_FILTER;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Raster filter for user {0}: {1}", new Object[] {
                    user.getName(), roiArea });
        }

        return roiArea;
    }

    MultiPolygon reprojectRoiArea(CoordinateReferenceSystem maskCrs,
            CoordinateReferenceSystem resourceCrs, MultiPolygon roiArea) {
        try {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Mask layer CRS: " + maskCrs.getName());
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Resource CRS: " + resourceCrs.getName());
            }
            if ((resourceCrs != null) && !CRS.equalsIgnoreMetadata(maskCrs, resourceCrs)) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Mask and Resource CRS differ, ROI area will be reprojected");
                }
                MathTransform mt = CRS.findMathTransform(maskCrs, resourceCrs, true);
                return (MultiPolygon) JTS.transform(roiArea, mt);
            } else {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Mask and Resource CRS coincide, no reprojection is necessary");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to reproject the restricted area to the layer's native SRS", e);
        }
        return roiArea;
    }

    List<String> getPermissions(Authentication user) throws IOException {
        List<String> permissions = dataAccessAdapter.getUserPermissions(user);
        if (permissions == null) {
            permissions = Collections.emptyList();
        }
        return permissions;
    }

    String getCqlFilterTemplate(Map<String, Object> options) {
        if (options == null || !options.containsKey(CQL_FILTER_TEMPLATE_OPTION)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(CQL_FILTER_TEMPLATE_OPTION
                        + " option not found, using default template");
            }
            return DEFAULT_CQL_FILTER_TEMPLATE;
        }
        return (String) options.get(CQL_FILTER_TEMPLATE_OPTION);
    }

    Name getMaskLayerName(Map<String, Object> options) {
        if (options == null || !options.containsKey(RASTER_MASK_LAYER_OPTION)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(RASTER_MASK_LAYER_OPTION
                        + " option not found, no raster filter will be created");
            }
            return null;
        }

        String rawName = (String) options.get(RASTER_MASK_LAYER_OPTION);
        Name qName = null;
        if (rawName != null) {
            int colonIdx = rawName.indexOf(":");
            boolean qualified = colonIdx >= 0;
            String ns = (qualified) ? rawName.substring(0, colonIdx) : null;
            String localPart = (qualified) ? rawName.substring(colonIdx + 1, rawName.length())
                    : rawName;
            qName = (qualified) ? new NameImpl(ns, localPart) : new NameImpl(localPart);
        }
        return qName;
    }

    String getMaskFilterTemplate(Map<String, Object> options) {
        if (options == null || !options.containsKey(RASTER_MASK_FILTER_TEMPLATE_OPTION)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(RASTER_MASK_FILTER_TEMPLATE_OPTION
                        + " option not found, using default template");
            }
            return DEFAULT_CQL_FILTER_TEMPLATE;
        }
        return (String) options.get(RASTER_MASK_FILTER_TEMPLATE_OPTION);
    }

    Filter buildCqlFilter(String cqlFilterTemplate, List<String> permissions) throws CQLException {
        return ECQL.toFilter(buildCqlPredicate(cqlFilterTemplate, permissions));
    }

    /**
     * Builds a CQL predicate by substituting a comma-separated list of permission tokens into the provided template.
     * 
     * <p>
     * The CQL filter template uses the same format as the {@link MessageFormat} class.
     * </p>
     * 
     * @param cqlFilterTemplate the CQL filter template
     * @param permissions the user permissions
     * @return a CQL predicate as a String
     */
    String buildCqlPredicate(String cqlFilterTemplate, List<String> permissions) {
        String commaSeparatedPermissions = StringUtils.join(permissions, ",");

        return MessageFormat.format(cqlFilterTemplate, commaSeparatedPermissions);
    }

    /**
     * Given a resource and a CQL filter, constructs a {@link DataAccessLimits} instance of a type matching the resouce's.
     * 
     * @param user the user
     * @param resource the resource
     * @param catalogMode the catalog mode
     * @param accessFilter the access filter (used as both read and write filter)
     * @return the data access limits
     */
    DataAccessLimits buildAccessLimitsInternal(Authentication user, ResourceInfo resource,
            CatalogMode catalogMode, Filter accessFilter) {
        if (resource instanceof FeatureTypeInfo) {
            return new VectorAccessLimits(catalogMode, null, accessFilter, null, accessFilter);
        } else if (resource instanceof CoverageInfo) {
            MultiPolygon rasterFilter = buildRasterFilter(user, resource);
            return new CoverageAccessLimits(catalogMode, accessFilter, rasterFilter, null);
        } else if (resource instanceof WMSLayerInfo) {
            MultiPolygon rasterFilter = buildRasterFilter(user, resource);
            // GetFeatureInfo is always allowed
            return new WMSAccessLimits(catalogMode, accessFilter, rasterFilter, true);
        } else {
            throw new IllegalArgumentException("Don't know how to handle resource of type: "
                    + resource.getClass().getName());
        }
    }

    @Override
    public String getDataSecurityToken(Authentication user, ResourceInfo resource) {
        Filter accessFilter = getAccessFilter(user);
        return new String(DigestUtils.md5Hex(accessFilter.toString()));
    }

    @Override
    public Map<String, Object> getOptions() {
        return new HashMap<String, Object>(options);
    }

    @Override
    public void setOptions(Map<String, Object> options) {
        if (options != null) {
            this.options = new HashMap<String, Object>(options);
        }
    }

}
