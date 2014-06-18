package org.jboss.certificate.tracker.core;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class KeystoreTrackingManagerTestCase {

    public static final String KEYSTORE_NAME = "server.keystore";
    public static final String KEYSTORE_FILE_PATH = getResourcesPath(KEYSTORE_NAME);
    public static final String KEYSTORE_PASS = "secret";

    @Test
    public void testAddAndRemoveManagers() {

        KeystoresTrackingManager trackingManager = KeystoresTrackingManager.INSTANCE;

        trackingManager.addKeystore(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS, null);

        Assert.assertNotNull(trackingManager.getTrustStore(KEYSTORE_NAME));

        trackingManager.removeKeystoreManager(KEYSTORE_NAME);
        
        Assert.assertNull(trackingManager.getTrustStore(KEYSTORE_NAME));

    }
    
    @Test
    public void testCertificateComparison() {

        KeystoresTrackingManager trackingManager = KeystoresTrackingManager.INSTANCE;
        JavaKeystoreManager keystoreManager = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS);
        X509Certificate certificate = keystoreManager.getCertByAlias("host1");
        
        X509Certificate updatedCertificate = JavaKeystoreManagerTestCase.loadCertificate(getResourcesPath("host1.cer"));
        
        CertificateInfo certificateInfo = new CertificateInfo(
                updatedCertificate.getSerialNumber().toString(),
                updatedCertificate.getSubjectDN().getName(),
                "VALID",  updatedCertificate.getType(),
                updatedCertificate.getVersion(),
                updatedCertificate.getNotBefore(),
                updatedCertificate.getNotAfter(),
                updatedCertificate.getNotBefore(),
                updatedCertificate.getIssuerDN().getName());
        
        // obtained certficate infos are in this form
        certificateInfo.setSubjectDN(certificateInfo.getSubjectDN().replaceAll(", ", ","));

        Assert.assertTrue(trackingManager.hasSameSubjectDN(certificate, certificateInfo));
        
        Assert.assertTrue(trackingManager.isUpdated(certificate, certificateInfo));

    }

    @Test
    public void testPKIClientSettings() {

        KeystoresTrackingManager trackingManager = KeystoresTrackingManager.INSTANCE;

        try {
            trackingManager.initPKIClient();
            Assert.fail();
        } catch (NullPointerException ex) {
            // OK, no class name was set
        }
        
        trackingManager.setClientName("BadClassName");
        
        try {
            trackingManager.initPKIClient();
            Assert.fail();
        } catch (NullPointerException ex) {
            // OK, wrong class name
        }

        trackingManager.setClientName("Dogtag");
        Map<String, Object> optionsMap = new HashMap<String, Object>();
        optionsMap.put("url", "http://example.com");

        trackingManager.setClientOptions(optionsMap);
        trackingManager.initPKIClient();

    }

    private static String getResourcesPath(String fileName) {

        return KeystoreTrackingManagerTestCase.class.getResource(fileName).getPath();
    }
}
