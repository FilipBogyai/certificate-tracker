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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.certificate.tracker.client.DogtagPKIClient;
import org.jboss.certificate.tracker.extension.CertificateTrackerLogger;

public class KeystoresTrackingManager {

    private final List<KeystoreManager> keystoreManagers;
    private PKIClient pkiClient;
    private String name;
    private Map<String, Object> options;
    private String module;

    public static final KeystoresTrackingManager INSTANCE = new KeystoresTrackingManager();

    private KeystoresTrackingManager() {
        pkiClient = null;
        options = null;
        keystoreManagers = new ArrayList<KeystoreManager>();
        name = null;
        module = null;
    }
    
    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setModule(String module) {
        this.module = module;
    }

    private void initPKIClient() {

        if (module == null) {

            if (name.equals(DogtagPKIClient.DOGTAG)) {
                pkiClient = new DogtagPKIClient();
                
            } else {
                ClassLoader classLoader = KeystoresTrackingManager.class.getClassLoader();
                pkiClient = PKIClientFactory.get(classLoader, name);
            }
               
        } else {
            pkiClient = PKIClientFactory.get(module, name);
        }
        pkiClient.init(options);

    }

    public KeyStore getTrustStore(String name) {

        if (name == null) {
            return null;
        }

        for (KeystoreManager manager : keystoreManagers) {
            if (manager.getName().equals(name)) {
                return manager.getTrustStore();
            }
        }
        return null;
    }

    public void addKeystore(String name, String keystorePath, String keystoreType, String password, String aliases) {
        
        KeystoreManager keystoreManager;
        if (aliases == null) {
            keystoreManager = new KeystoreManager(name, keystorePath, keystoreType, password);
        } else {
            keystoreManager = new KeystoreManager(name, keystorePath, keystoreType, password, aliases);
        }
        addKeystoreManager(keystoreManager);
    }
    
    public void addKeystoreManager(KeystoreManager keystoreManager){
        
        keystoreManagers.add(keystoreManager);
    }
    
    public void removeKeystoreManager(String name) {

        for (KeystoreManager keystoreManager : keystoreManagers) {

            if (keystoreManager.getName().equals(name)) {
                keystoreManagers.remove(keystoreManager);
                return;
            }
        }
    }

    public void updateAllKeystores() throws IOException {

        if (pkiClient == null) {
            initPKIClient();
        }
        Collection<CertificateInfo> certificateInfos = pkiClient.listCertificates();
        
        for(KeystoreManager manager : keystoreManagers){
            
            updateCertificates(manager, certificateInfos);
        }
    }

    public void updateCertificates(KeystoreManager manager, Collection<CertificateInfo> certificateInfos) throws IOException {

        List<X509Certificate> managedCertificates = new ArrayList<X509Certificate>();
        try {
            managedCertificates = manager.getManagedKeystoreCertificates();
        } catch (KeyStoreException ex) {
            CertificateTrackerLogger.LOGGER.unableToLoadCertificates(manager.getName(), ex);
        }
        
        for(X509Certificate certificate: managedCertificates){
            for (CertificateInfo certificateInfo : certificateInfos) {

                if (hasSameSubjectDN(certificate, certificateInfo) && isUpdated(certificate, certificateInfo)) {

                    X509Certificate newCertificate = pkiClient.getCertificate(certificateInfo.getAlias());
                    try {
                        manager.replaceCertificate(certificate, newCertificate);
                    } catch (Exception ex) {
                        CertificateTrackerLogger.LOGGER.unableToUpdateCertificate(certificate.getSubjectDN().getName(), 
                                manager.getName(), ex);
                    }
                }
            }
        }
            
        if (manager.isUpdated()) {
            manager.saveKeystore();
            // TODO
            ServerKeystoreReload.INSTANCE.reloadKeystore(manager.getKeystorePath());
        }

    }
    
    public boolean hasSameSubjectDN(X509Certificate certificate, CertificateInfo certificateInfo) {
        
        String subjectDNString = certificate.getSubjectDN().toString().replaceAll(", ", ",");
        return subjectDNString.equals(certificateInfo.getSubjectDN());

    }

    public boolean isUpdated(X509Certificate certificate, CertificateInfo certificateInfo) {

        boolean hasLongerValidity = certificate.getNotAfter().compareTo(certificateInfo.getNotValidAfter()) < 0;

        boolean isValid = (certificateInfo.getNotValidBefore().getTime() < System.currentTimeMillis())
                && (certificateInfo.getNotValidAfter().getTime() > System.currentTimeMillis());

        if (!(certificateInfo.getStatus() == null || certificateInfo.getStatus().isEmpty())) {
            isValid = isValid && certificateInfo.getStatus().equals("VALID");
        }

        return hasLongerValidity && isValid;
    }
    

}
