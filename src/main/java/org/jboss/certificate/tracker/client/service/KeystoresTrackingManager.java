package org.jboss.certificate.tracker.client.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.logging.Logger;

public class KeystoresTrackingManager {

    private final List<KeystoreManager> keystoreManagers;
    private PKIClient pkiClient;
    private String urlTarget;

    private final Logger log = Logger.getLogger(KeystoresTrackingManager.class);

    public static final KeystoresTrackingManager INSTANCE = new KeystoresTrackingManager();

    private KeystoresTrackingManager() {
        pkiClient = null;
        keystoreManagers = new ArrayList<KeystoreManager>();
    }

    public KeystoresTrackingManager(PKIClient pkiClient) {
        
        this.pkiClient = pkiClient;
        keystoreManagers = new ArrayList<KeystoreManager>();
    }
    
    public void setNewDogtagClient(String urlTarget) throws URISyntaxException {

        pkiClient = new DogtagPKIClient(urlTarget);

    }

    public void setUrlTarget(String urlTarget) {
        this.urlTarget = urlTarget;
    }

    public void setPKIClient(PKIClient pkiClient) {
        this.pkiClient = pkiClient;
    }

    private void initPKIClient() {

        try {
            setNewDogtagClient(urlTarget);
        } catch (URISyntaxException ex) {
            log.error("URL of CA is wrong: " + ex);
        }
    }

    public void addKeystore(String keystorePath, String keystoreType, String password, String aliases) {
        
        KeystoreManager keystoreManager = new KeystoreManager(keystorePath, keystoreType, password, aliases);
        addKeystoreManager(keystoreManager);
    }
    
    public void addKeystoreManager(KeystoreManager keystoreManager){
        
        keystoreManagers.add(keystoreManager);
    }
    
    public KeystoreManager getKeystoreManager(String path) {

        for (KeystoreManager keystoreManager : keystoreManagers) {

            if (keystoreManager.getKeystorePath().equals(path)) {
                return keystoreManager;
            }
        }
        return null;

    }

    public void removeKeystoreManager(String path) {

        for (KeystoreManager keystoreManager : keystoreManagers) {

            if (keystoreManager.getKeystorePath().equals(path)) {
                keystoreManagers.remove(keystoreManager);
                return;
            }
        }
    }

    public void updateAllKeystores() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
        FileNotFoundException, IOException {

        if (pkiClient == null) {
            initPKIClient();
        }
        Collection<CertificateInfo> certificateInfos = pkiClient.listCerts();
        
        for(KeystoreManager manager : keystoreManagers){
            
            updateCertificates(manager, certificateInfos);
        }
    }

    public void updateCertificates(KeystoreManager manager, Collection<CertificateInfo> certificateInfos) throws KeyStoreException,
            NoSuchAlgorithmException,
            CertificateException, FileNotFoundException, IOException {
        
        List<X509Certificate> managedCertificates = manager.getManagedKeystoreCertificates();
        boolean certificatesChanged = false;

        log.info("This is certificate count: " + certificateInfos.size());
        for(X509Certificate certificate: managedCertificates){
            for (CertificateInfo certificateInfo : certificateInfos) {

                if (isUpdated(certificate, certificateInfo)) {

                    X509Certificate newCertificate = pkiClient.getCert(certificateInfo.getAlias());
                    manager.replaceCertificate(certificate, newCertificate);
                    System.out.println("Zmena?");
                    certificatesChanged = true;
                }
            }
        }
            
        if (certificatesChanged) {
            manager.saveKeystore();
            // TODO
            ReloadKeystoreService.INSTANCE.checkSecurityRealms(manager.getKeystorePath());
        }

    }
    
    public boolean isUpdated(X509Certificate certificate, CertificateInfo certificateInfo) {
        
        if (getSubjectDNString(certificate).equals(certificateInfo.getSubjectDN())) {
            System.out.println("Zhoda-" + getSubjectDNString(certificate) + certificate.getNotAfter() + " oproti "
                    + certificateInfo.getNotValidAfter());
            if (certificate.getNotAfter().compareTo(certificateInfo.getNotValidAfter()) < 0) {
                return true;
            }

        }

        return false;
    }

    public String getSubjectDNString(X509Certificate certificate) {

        return certificate.getSubjectDN().toString().replaceAll(", ", ",");
    }

}
