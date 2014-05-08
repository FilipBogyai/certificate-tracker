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

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

public interface KeystoreManager {

    /**
     * Getter of keystore manager name
     * 
     * @return name of keystore manager
     */
    public String getName();
    
    /**
     * Getter of keystore file path
     * 
     * @return file path of managed keystore
     */
    public String getKeystorePath();
    
    /**
     * This method is for checking if any entry in keystore has changed
     * since last save. 
     * 
     * @return boolean whether any entry has been changed since last save
     */
    public boolean isChanged();
    
    /**
     * Creates truststore with copied certificate entries from managed keystore.
     * 
     * @return truststore with certificate entries
     */
    public KeyStore getTrustStore();
    
    /**
     * Gets aliases (entries names) from the keystore.
     * 
     * @return array of keystore aliases
     */
    public String[] getKeystoreAliases();

    /**
     * Gets the certificate associated with the given alias.
     * 
     * @param alias name of requested certificate
     * 
     * @return the certificate, or null if the given alias does not exist or
     *         does not contain a certificate.
     */
    public X509Certificate getCertByAlias(String alias);

    /**
     * Gets all certificates from the keystore.
     * 
     * @return list of all keystore X509 certificates
     */
    public List<X509Certificate> getAllCertificates();
    
    /**
     * Gets only managed certificates from the keystore. 
     * 
     * @return list of managed keystore X509 certificates
     */
    public List<X509Certificate> getManagedCertificates();
    
    /**
     * Replace old certificate with new certificate in keystore
     * 
     * @param oldCertificate which will be replaced
     * @param newCertificate as a replacement
     * 
     */
    public void replaceCertificate(X509Certificate oldCertificate, X509Certificate newCertificate);
    
    /**
     * Saves keystore to file determined by keystorePath.
     * 
     */
    public void saveKeystore();
    
    
}
