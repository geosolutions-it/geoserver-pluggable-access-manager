package it.geosolutions.geoserver.security.impl;

import it.geosolutions.geoserver.security.AccessLimitsBuilder;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.security.CatalogMode;
import org.geoserver.security.CoverageAccessLimits;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.VectorAccessLimits;
import org.geoserver.security.WMSAccessLimits;
import org.opengis.filter.Filter;
import org.springframework.security.core.Authentication;

/**
 * Simple access limits builder that can be configured to allow/deny read/write on a resource.
 * 
 * @author Lorenzo Natali, GeoSolutions
 *
 */
public class ReadWriteAccessLimitsBuilder implements AccessLimitsBuilder {

    private boolean canRead = true;

    private boolean canWrite = false;

    @Override
    public DataAccessLimits buildAccessLimits(Authentication user, ResourceInfo resource,
            CatalogMode catalogMode) {
        // basic filter
        Filter readFilter = getReadFilter();
        Filter writeFilter = getWriteFilter();

        if (resource instanceof FeatureTypeInfo) {
            ((FeatureTypeInfo) resource).getAttributes();
            return new VectorAccessLimits(catalogMode, null, readFilter, null, writeFilter);
        } else if (resource instanceof CoverageInfo) {
            return new CoverageAccessLimits(catalogMode, readFilter, null, null);
        } else if (resource instanceof WMSLayerInfo) {
            return new WMSAccessLimits(catalogMode, readFilter, null, true);
        } else {
            throw new IllegalArgumentException("Don't know how to handle resource " + resource);
        }
    }

    @Override
    public String getDataSecurityToken(Authentication user, ResourceInfo resource) {
        StringBuilder sb = new StringBuilder();
        sb.append(getReadFilter().toString()).append("_").append(getWriteFilter().toString());

        return DigestUtils.md5Hex(sb.toString());
    }

    @Override
    public Map<String, Object> getOptions() {
        return Collections.emptyMap();
    }

    @Override
    public void setOptions(Map<String, Object> options) {
        // no-op: this access limit builder has no configuration options
    }

    Filter getReadFilter() {
        return canRead ? Filter.INCLUDE : Filter.EXCLUDE;
    }

    Filter getWriteFilter() {
        return canWrite ? Filter.INCLUDE : Filter.EXCLUDE;
    }

    /**
     * @return the canRead
     */
    public boolean isCanRead() {
        return canRead;
    }

    /**
     * @param canRead the canRead to set
     */
    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    /**
     * @return the canWrite
     */
    public boolean isCanWrite() {
        return canWrite;
    }

    /**
     * @param canWrite the canWrite to set
     */
    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

}
