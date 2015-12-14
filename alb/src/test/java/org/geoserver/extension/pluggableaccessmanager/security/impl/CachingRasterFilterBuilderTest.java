package org.geoserver.extension.pluggableaccessmanager.security.impl;

import static it.geosolutions.geoserver.security.impl.PluggableAccessManagerTestUtils.copyConfigurationFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;

import org.geoserver.data.test.SystemTestData;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.wms.WMSTestSupport;
import org.junit.Test;

import com.mockrunner.mock.web.MockHttpServletResponse;

public class CachingRasterFilterBuilderTest extends WMSTestSupport {

    private static final String CONFIG_FILE = "/test-config-alb.xml";

    private static final String NAMESPACE = "http://www.geo-solutions.it/test";

    private static final String PREFIX = "test";

    private static final String RASTER_FILTER_LAYER = "raster_filter_tazbm";

    private static final QName QNAME_RASTER_FILTER_LAYER = new QName(NAMESPACE,
            RASTER_FILTER_LAYER, PREFIX);

    private static final String GET_MAP = "wms?" + "service=WMS&" + "version=1.1.0&"
            + "request=GetMap&" + "layers=" + SystemTestData.TASMANIA_BM.getPrefix() + ":"
            + SystemTestData.TASMANIA_BM.getLocalPart() + "&"
            + "bbox=146.49999999999477,-44.49999999999785,147.99999999999474,-42.99999999999787&"
            + "width=767&" + "height=768&" + "srs=EPSG:4326&" + "format=image/png";

    private static final int[] EXP_RESTRICTED_PIXEL = new int[] { 27, 69, 125 };

    private static final int[] EXP_FIRST_POLY_PIXEL = new int[] { 52, 73, 12 };

    private static final int[] EXP_SECOND_POLY_PIXEL = new int[] { 26, 70, 124 };

    // private static final int[] BLACK_PIXEL = new int[] { 0, 0, 0 };
    private static final int[] WHITE_PIXEL = new int[] { 255, 255, 255 };

    @Override
    protected void setUpTestData(SystemTestData testData) throws Exception {
        super.setUpTestData(testData);

        copyConfigurationFile(testData.getDataDirectoryRoot(), CONFIG_FILE);
    }

    @Override
    protected void setUpSpring(List<String> springContextLocations) {
        super.setUpSpring(springContextLocations);

        springContextLocations.add("classpath:/testApplicationContext.xml");
    }

    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);

        testData.addDefaultRasterLayer(SystemTestData.TASMANIA_BM, getCatalog());
        testData.addVectorLayer(QNAME_RASTER_FILTER_LAYER, getCatalog());
    }

    @Test
    public void testPublished() {
        assertNotNull(getCatalog().getLayerByName(RASTER_FILTER_LAYER));
        // login as a user who can see the raster layer
        login("bob", "password");
        assertNotNull(getCatalog().getLayerByName(SystemTestData.TASMANIA_BM.getLocalPart()));
    }

    @Test
    public void testAdminAccess() throws Exception {
        login("admin", "geoserver", GeoServerRole.ADMIN_ROLE.getAuthority());

        MockHttpServletResponse response = getAsServletResponse(GET_MAP);

        assertEquals("image/png", response.getContentType());
        BufferedImage image = ImageIO.read(getBinaryInputStream(response));
        assertNotNull(image);
        assertNotBlank("testAdminAccess", image);

        // check the colors of some pixels to ensure there has been no filtering
        checkRestrictedPixel(image, false);
        checkFirstPolygonPixel(image, false);
        checkSecondPolygonPixel(image, false);
    }

    @Test
    public void testAccessBoth() throws Exception {
        // login as bob (who has access to both restricted areas)
        login("bob", "password");

        MockHttpServletResponse response = getAsServletResponse(GET_MAP);

        assertEquals("image/png", response.getContentType());
        BufferedImage image = ImageIO.read(getBinaryInputStream(response));
        assertNotNull(image);
        assertNotBlank("testFilteredAccess", image);

        checkRestrictedPixel(image, true);
        checkFirstPolygonPixel(image, false);
        checkSecondPolygonPixel(image, false);
    }

    @Test
    public void testAccessJustOne() throws Exception {
        // login as alice (who has access just to the first restricted area)
        login("alice", "password");

        MockHttpServletResponse response = getAsServletResponse(GET_MAP);

        assertEquals("image/png", response.getContentType());
        BufferedImage image = ImageIO.read(getBinaryInputStream(response));
        assertNotNull(image);
        assertNotBlank("testFilteredAccess", image);

        checkRestrictedPixel(image, true);
        checkFirstPolygonPixel(image, false);
        checkSecondPolygonPixel(image, true);
    }

    @Test
    public void testAccessNone() throws Exception {
        // login as sam (who has no access at all)
        login("sam", "password");

        MockHttpServletResponse response = getAsServletResponse(GET_MAP);

        assertEquals("image/png", response.getContentType());
        BufferedImage image = ImageIO.read(getBinaryInputStream(response));
        assertNotNull(image);
        assertBlank("testFilteredAccess", image);
    }

    private void checkRestrictedPixel(BufferedImage image, boolean isWhite) {
        checkPixel(image, 586, 414, (isWhite) ? WHITE_PIXEL : EXP_RESTRICTED_PIXEL);
    }

    private void checkFirstPolygonPixel(BufferedImage image, boolean isWhite) {
        checkPixel(image, 432, 130, (isWhite) ? WHITE_PIXEL : EXP_FIRST_POLY_PIXEL);
    }

    private void checkSecondPolygonPixel(BufferedImage image, boolean isWhite) {
        checkPixel(image, 148, 415, (isWhite) ? WHITE_PIXEL : EXP_SECOND_POLY_PIXEL);
    }

    private void checkPixel(BufferedImage image, int x, int y, int[] expectedRGB) {
        int[] pixel = new int[4];
        image.getData().getPixel(x, y, pixel);
        assertEquals(expectedRGB[0], pixel[0]);
        assertEquals(expectedRGB[1], pixel[1]);
        assertEquals(expectedRGB[2], pixel[2]);
    }

}