package org.jboss.certificate.tracker.core;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.certificate.tracker.client.DogtagPKIClient;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;

public class KeystoresTrackingManager {

    private final List<KeystoreManager> keystoreManagers;
    private PKIClient pkiClient;
    private String urlTarget;
    private String trustStoreManagerName;
    private String name;
    private String module;

    private final Logger log = Logger.getLogger(KeystoresTrackingManager.class);

    public static final KeystoresTrackingManager INSTANCE = new KeystoresTrackingManager();

    private KeystoresTrackingManager() {
        pkiClient = null;
        trustStoreManagerName = null;
        keystoreManagers = new ArrayList<KeystoreManager>();
        name = null;
        module = null;
    }
    
    public void setUrlTarget(String urlTarget) {
        this.urlTarget = urlTarget;
    }

    public void setTrustStoreManagerName(String trustStoreManagerName) {
        this.trustStoreManagerName = trustStoreManagerName;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setModule(String module) {
        this.module = module;
    }

    private void initPKIClient() {
        
        KeyStore trustStore = null;
        if (!(trustStoreManagerName == null || trustStoreManagerName.isEmpty())) {
             trustStore = getKeystoreManager(trustStoreManagerName).getTrustStore();
        }        

        if (name.equals(DogtagPKIClient.DOGTAG)) {
            pkiClient = new DogtagPKIClient();
            pkiClient.init(urlTarget, trustStore);
                
        } else if (module == null) {
                            
            ClassLoader classLoader = KeystoresTrackingManager.class.getClassLoader();
            pkiClient = PKIClientFactory.get(classLoader, name);
            pkiClient.init(urlTarget, trustStore);
               
        } else {
            try {
                ModuleLoader loader = Module.getCallerModuleLoader();
                Module customModule = loader.loadModule(ModuleIdentifier.fromString(module));
                ClassLoader classLoader = customModule.getClassLoader();
                pkiClient = PKIClientFactory.get(classLoader, name);
            } catch (ModuleLoadException ex) {
                log.error("Cannot load module for custom PKIClient", ex);
            }
                
            pkiClient.init(urlTarget, trustStore);
        }

    }

    private KeystoreManager getKeystoreManager(String name) {
        for (KeystoreManager manager : keystoreManagers) {
            if (manager.getName().equals(name)) {
                return manager;
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
            log.error("Cannot obtain keystore certificates:" + manager.getKeystorePath(), ex);
        }
        
        for(X509Certificate certificate: managedCertificates){
            for (CertificateInfo certificateInfo : certificateInfos) {

                if (hasSameSubjectDN(certificate, certificateInfo) && isUpdated(certificate, certificateInfo)) {

                    X509Certificate newCertificate = pkiClient.getCertificate(certificateInfo.getAlias());
                    try {
                        manager.replaceCertificate(certificate, newCertificate);
                    } catch (Exception ex) {
                        log.error("Unable to update certificate: " + certificate.getSubjectDN().getName(), ex);
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
