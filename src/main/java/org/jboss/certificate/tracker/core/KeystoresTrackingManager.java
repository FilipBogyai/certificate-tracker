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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.certificate.tracker.client.DogtagPKIClient;

/**
 * This class is responsible for tracking all certificates in managed keystores.
 * It uses {@link KeystoreManager} for manipulation of certificates in each
 * keystore. Certificates are synchronized with {@link PKIClient}, which
 * provides methods for getting actual certificates from Certificate System.
 * 
 * @author Filip Bogyai
 */
public class KeystoresTrackingManager {

    private final List<KeystoreManager> keystoreManagers;
    private PKIClient pkiClient;
    private String clientClassName;
    private Map<String, Object> clientOptions;
    private String clientModule;

    //This class is Singleton
    public static final KeystoresTrackingManager INSTANCE = new KeystoresTrackingManager();

    /**
     * Constructor of KeystoresTrackingManager Singleton
     *     
     */
    private KeystoresTrackingManager() {
        pkiClient = null;
        clientOptions = null;
        keystoreManagers = new ArrayList<KeystoreManager>();
        clientClassName = null;
        clientModule = null;
    }
    
    /**
     * Setter of customizable client options for {@link PKIClient}
     * 
     * @param options map of key/value
     */
    public void setClientOptions(Map<String, Object> options) {
        this.clientOptions = options;
    }
    
    /**
     * Setter of {@link PKIClient} fully qualified class name  
     * 
     * @param clientClassName is a name of class which implements {@link PKIClient}
     */
    public void setClientName(String clientClassName) {
        this.clientClassName = clientClassName;
    }
    
    /**
     * Setter of {@link PKIClient} module
     * 
     * @param clientModule is a name of module where the implementation of {@link PKIClient} is
     */
    public void setClientModule(String clientModule) {
        this.clientModule = clientModule;
    }

    /**
     * Initializes {@link PKIClient}, which is loaded by provided class name/module.
     * Then the client is initialized with custom clientOptions.   
     */
    public void initPKIClient() {

        if (clientModule == null) {

            if (clientClassName.equals(DogtagPKIClient.DOGTAG)) {
                pkiClient = new DogtagPKIClient();
                
            } else {
                ClassLoader classLoader = KeystoresTrackingManager.class.getClassLoader();
                pkiClient = PKIClientFactory.get(classLoader, clientClassName);
            }
               
        } else {
            pkiClient = PKIClientFactory.getFromModule(clientModule, clientClassName);
        }
        pkiClient.init(clientOptions);

    }

    /**
     * Getter for truststore which is obtained from defined {@link KeystoreManager} 
     *   
     * @param name of KeystoreManager from which truststore should be obtained
     */
    public KeyStore getTrustStore(String nameOfManager) {

        if (nameOfManager == null) {
            return null;
        }

        for (KeystoreManager manager : keystoreManagers) {
            if (manager.getName().equals(nameOfManager)) {
                return manager.getTrustStore();
            }
        }
        return null;
    }

    /**
     * Adds managed keystore by creating new {@link JavaKeystoreManager}
     * 
     * @param name of keystore manager which will be created
     * @param keystorePath file path of keystore
     * @param keystoreType type of keystore
     * @param password of keystore
     * @param aliases comma separated name of certificate aliases which should be managed
     */
    public void addKeystore(String name, String keystorePath, String keystoreType, String password, String aliases) {
        
        KeystoreManager keystoreManager;
        if (aliases == null) {
            keystoreManager = new JavaKeystoreManager(name, keystorePath, keystoreType, password);
        } else {
            keystoreManager = new JavaKeystoreManager(name, keystorePath, keystoreType, password, aliases);
        }
        addKeystoreManager(keystoreManager);
    }
    
    /**
     * Adds {@link KeystoreManager} to list of managers
     *   
     * @param keystoreManager which will be managed 
     */
    public void addKeystoreManager(KeystoreManager keystoreManager){
        
        keystoreManagers.add(keystoreManager);
    }
    
    /**
     * Removes {@link KeystoreManager} from list of managers
     *   
     * @param keystoreManager which will be removed  
     */
    public void removeKeystoreManager(String name) {

        for (KeystoreManager keystoreManager : keystoreManagers) {

            if (keystoreManager.getName().equals(name)) {
                keystoreManagers.remove(keystoreManager);
                return;
            }
        }
    }

    /**
     * Compares and updates certificates obtained from {@link PKIClient} with
     *  each {@link KeystoreManager} in list of managers
     */
    public void updateAllKeystores() throws IOException {

        if (pkiClient == null) {
            initPKIClient();
        }
        Collection<CertificateInfo> certificateInfos = pkiClient.listCertificates();
        
        for(KeystoreManager manager : keystoreManagers){
            
            updateKeystoreCertificates(manager, certificateInfos);
        }
    }

    /**
     * Compares and updates certificates obtained from {@link PKIClient} with managed certificates 
     * from single {@link KeystoreManager}. If updated certificate is found, then it is replaced 
     * in managed keystore.
     * 
     *  @param keystoreManager which will be checked for updates
     *  @param certificateInfos list of all certificate information obtained from {@link PKIClient}
     * 
     */
    public void updateKeystoreCertificates(KeystoreManager manager, Collection<CertificateInfo> certificateInfos) throws IOException {

        List<X509Certificate> managedCertificates = manager.getManagedCertificates();

        for(X509Certificate certificate: managedCertificates){
            for (CertificateInfo certificateInfo : certificateInfos) {

                if (hasSameSubjectDN(certificate, certificateInfo) && isUpdated(certificate, certificateInfo)) {

                    X509Certificate newCertificate = pkiClient.getCertificate(certificateInfo.getAlias());
                    manager.replaceCertificate(certificate, newCertificate);
                }

            }
        }
            
        if (manager.isChanged()) {
            manager.saveKeystore();            
            ServerServicesReload.INSTANCE.reloadDependentServices(manager.getKeystorePath());
        }

    }
    
    /**
     * Compares Subject name from {@link X509Certificate} with Subject name from {@link CertificateInfo}
     * 
     * @return boolean if the name is same
     */
    public boolean hasSameSubjectDN(X509Certificate certificate, CertificateInfo certificateInfo) {
        
        String subjectDNString = certificate.getSubjectDN().toString().replaceAll(", ", ",");
        return subjectDNString.equals(certificateInfo.getSubjectDN());

    }

    /**
     * Determine if certificate from managed keystore can be updated. This compares information 
     * from {@link X509Certificate} with {@link CertificateInfo}.
     * 
     * @param certificate from managed keystore
     * @param certificateInfo possible updated version of certificate
     * 
     * @return boolean if the updated certificate is available
     */
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
