/** Copyright 2014 Filip Bogyai
 *
 * This file is part of certificate-tracker.
 *
 * Certificate-tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.jboss.certificate.tracker.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.jboss.util.file.Files;
import org.junit.Assert;
import org.junit.Test;

public class JavaKeystoreManagerTestCase {

    public static final String KEYSTORE_NAME = "server.keystore";
    public static final String KEYSTORE_FILE_PATH = getResourcesPath(KEYSTORE_NAME);
    public static final String KEYSTORE_PASS = "secret";

    @Test
    public void testKeystoreInitialization() {
      
        System.out.println(KEYSTORE_FILE_PATH);

        //inicialization of manager for all keystore certificates
        JavaKeystoreManager manager = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS);

        Assert.assertEquals(manager.getName(), KEYSTORE_NAME);
        Assert.assertEquals(manager.getKeystorePath(), KEYSTORE_FILE_PATH);

        Assert.assertEquals(manager.getAllCertificates().size(), 2);
        Assert.assertEquals(manager.getManagedCertificates().size(), 2);
        
        //inicialization of manager with only one certificate managed under alias "server" 
        JavaKeystoreManager manager2 = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS, "server");     
        Assert.assertEquals(manager2.getAllCertificates().size(), 2);
        Assert.assertEquals(manager2.getManagedCertificates().size(), 1);
        
        //inicialization of manager with only both certificate listed as managed 
        JavaKeystoreManager manager3 = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS, "server,host1");     
        Assert.assertEquals(manager3.getAllCertificates().size(), 2);
        Assert.assertEquals(manager3.getManagedCertificates().size(), 2);
        
    }
    
    @Test
    public void testKeystoreAliases() {

        JavaKeystoreManager manager = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS);

        Assert.assertEquals(manager.getKeystoreAliases().length, 2);
        Assert.assertEquals(manager.getKeystoreAliases()[0], "host1");
        Assert.assertEquals(manager.getKeystoreAliases()[1], "server");

    }
    
    @Test
    public void testGetCertByAlias() {

        JavaKeystoreManager manager = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS);

        X509Certificate existingCert = manager.getCertByAlias("server");
        Assert.assertNotNull(existingCert);

        X509Certificate nonExistingCert = manager.getCertByAlias("foo");
        Assert.assertNull(nonExistingCert);

    }

    @Test
    public void testReplaceCertificates() {

        JavaKeystoreManager manager = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS);

        Assert.assertFalse(manager.isChanged());

        X509Certificate oldCertificate = manager.getCertByAlias("host1");
        X509Certificate newCertificate = loadCertificate(getResourcesPath("host1.cer"));

        manager.replaceCertificate(oldCertificate, newCertificate);
        X509Certificate updatedCertificate = manager.getCertByAlias("host1");

        Assert.assertNotSame(oldCertificate, updatedCertificate);
        Assert.assertTrue(manager.isChanged());

    }
    
    @Test
    public void testSaveKeystore() throws IOException {

        // create copy of original keystore
        File keystoreFile = new File(KEYSTORE_FILE_PATH);
        File originalFile = File.createTempFile("original", "keystore");
        Files.copy(keystoreFile, originalFile);
        
        X509Certificate newCertificate = loadCertificate(getResourcesPath("host1.cer"));

        JavaKeystoreManager manager = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS);

        X509Certificate oldCertificate = manager.getCertByAlias("host1");
        manager.replaceCertificate(oldCertificate, newCertificate);
        X509Certificate updatedCertificate = manager.getCertByAlias("host1");
        Assert.assertNotSame(oldCertificate, updatedCertificate);

        // without save the new created manager has loaded old certificate
        JavaKeystoreManager manager2 = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS);

        X509Certificate oldCertificate2 = manager2.getCertByAlias("host1");
        Assert.assertSame(oldCertificate2, oldCertificate);
        Assert.assertNotSame(oldCertificate2, newCertificate);

        // now save the first manager with updated certficate
        manager.saveKeystore();

        // after save the new created manager has loaded updated certificate
        JavaKeystoreManager manager3 = new JavaKeystoreManager(KEYSTORE_NAME, KEYSTORE_FILE_PATH, "JKS", KEYSTORE_PASS);

        X509Certificate updatedCertificate3 = manager3.getCertByAlias("host1");
        Assert.assertNotSame(updatedCertificate3, oldCertificate);
        Assert.assertSame(updatedCertificate3, newCertificate);

        // replace changed keystore by original
        Files.copy(originalFile, keystoreFile);

        originalFile.delete();

    }
    
    /**
     * Loads a {@link X509Certificate} from the given path. Returns null if the
     * certificate can't be loaded.
     * 
     * @param filePath
     * @return
     */
    public static X509Certificate loadCertificate(final String filePath) {
        if (filePath.isEmpty()) {

            return null;
        }
        FileInputStream inStream = null;
        X509Certificate cert = null;
        try {
            final CertificateFactory certFac = CertificateFactory.getInstance("X.509"); // X.509
            inStream = new FileInputStream(new File(filePath));
            cert = (X509Certificate) certFac.generateCertificate(inStream);
        } catch (Exception ex) {

        } finally {
            try {
                inStream.close();
            } catch (IOException ex) {
                // OK
            }
        }
        return cert;
    }

    private static String getResourcesPath(String fileName) {

        return JavaKeystoreManagerTestCase.class.getResource(fileName).getPath();
    }
}
