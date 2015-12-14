package it.geosolutions.geoserver.security.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class PluggableAccessManagerTestUtils {

    public static final String CONFIG_FILE_DEST = "pluggable-access-manager.xml";

    public static void copyConfigurationFile(File dataDirRoot, String configResource)
            throws IOException {
        // copy configuration to data directory
        assertTrue(dataDirRoot.canWrite());
        File securityDir = new File(dataDirRoot, "security");
        assertNotNull(securityDir);
        if (!securityDir.exists()) {
            assertTrue(securityDir.mkdir());
        }
        assertTrue(securityDir.canWrite());
        File configFile = new File(securityDir, CONFIG_FILE_DEST);
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            IOUtils.copy(PluggableAccessManagerTestUtils.class.getResourceAsStream(configResource),
                    fos);
        }
    }

    public static Authentication getLoggedInUser() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
