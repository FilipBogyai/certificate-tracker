package org.jboss.certificate.tracker.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jboss.certificate.tracker.extension.CertificateTrackerLogger;

public class KeystoreManager {

    private final String name;
    private final String keystorePath;
    private final char[] password;
    private final KeyStore keystore;
    private final String[] managedAliases;
    private boolean isUpdated;

    public KeystoreManager(String name, String keystorePath, String keystoreType, String password) {

        this(name, keystorePath, keystoreType, password, "");
    }

    public KeystoreManager(String name, String keystorePath, String keystoreType, String password, String aliases) {

        this(name, keystorePath, keystoreType, password, aliases.split(","));
    }

    public KeystoreManager(String name, String keystorePath, String keystoreType, String password, String[] managedAliases) {

        this.name = name;
        this.keystorePath = keystorePath;
        this.password = password.toCharArray();
        this.managedAliases = managedAliases;
        this.isUpdated = false;

        keystore = loadKeyStore(keystoreType, keystorePath, this.password);

    }

    public String getName() {
        return name;
    }

    public String getKeystorePath() {
        return keystorePath;
    }
    
    public boolean isUpdated() {
        return isUpdated;
    }

    public KeyStore getTrustStore() {

        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance("JKS");
            trustStore.load(null, null);
            copyCertificates(trustStore);
        } catch (Exception ex) {
            CertificateTrackerLogger.LOGGER.unableToCreateTruststore(name, ex);
        }
        return trustStore;
    }

    /**
     * Loads certificate names (aliases) from the initialized keystore
     * 
     * @return array of certificate aliases
     */
    public String[] getKeystoreAliases() {

        List<String> aliases = new ArrayList<String>();
        try {
            Enumeration<String> aliasEnum = keystore.aliases();
            while (aliasEnum.hasMoreElements()) {
                aliases.add(aliasEnum.nextElement());
            }
        } catch (KeyStoreException ex) {
            CertificateTrackerLogger.LOGGER.cannotLoadCertificates(ex);
        }

        return aliases.toArray(new String[aliases.size()]);
    }

    public X509Certificate getCertByAlias(String alias) throws KeyStoreException {

        if (keystore.getCertificate(alias).getType().equals("X.509")) {
            return (X509Certificate) keystore.getCertificate(alias);
        }
        return null;
    }

    public List<X509Certificate> getAllKeystoreCertificates() throws KeyStoreException {

        List<X509Certificate> keystoreCertificates = new ArrayList<X509Certificate>();
        for (String alias : getKeystoreAliases()) {
            keystoreCertificates.add(getCertByAlias(alias));
        }
        return keystoreCertificates;

    }

    public List<X509Certificate> getManagedKeystoreCertificates() throws KeyStoreException {

        if (managedAliases == null || managedAliases[0].isEmpty()) {
            return getAllKeystoreCertificates();
        }

        List<X509Certificate> keystoreCertificates = new ArrayList<X509Certificate>();
        for (int i = 0; i < managedAliases.length; i++) {
            try {
                X509Certificate certificate = getCertByAlias(managedAliases[i]);
                keystoreCertificates.add(certificate);
            } catch (NullPointerException ex) {
                CertificateTrackerLogger.LOGGER.noCertificateWithAlias(managedAliases[i], name);
            }

        }

        return keystoreCertificates;

    }

    public void replaceCertificate(X509Certificate oldCertificate, X509Certificate newCertificate) throws KeyStoreException,
            UnrecoverableKeyException, NoSuchAlgorithmException {
        
        String alias = keystore.getCertificateAlias(oldCertificate);
        
        if (keystore.isKeyEntry(alias)) {

            if (oldCertificate.getPublicKey().equals(newCertificate.getPublicKey())) {
                setKeyEntryWithCertificate(alias, newCertificate);
                isUpdated = true;
                CertificateTrackerLogger.LOGGER.updatedKeyCertificate(alias, name);
            } else {
                CertificateTrackerLogger.LOGGER.differentKeyCertificate(oldCertificate.getSubjectDN().getName());
            }
        } else {
            keystore.setCertificateEntry(alias, newCertificate);
            isUpdated = true;
            CertificateTrackerLogger.LOGGER.updatedCertificate(alias, name);
        }
    }

    public void saveKeystore() {

        File keystoreFile = new File(keystorePath);
        try {
            keystore.store(new FileOutputStream(keystoreFile), password);
            isUpdated = false;
        } catch (Exception ex) {
            CertificateTrackerLogger.LOGGER.unableToSaveKeystore(name, ex);
        }

    }

    public void setKeyEntryWithCertificate(String alias, X509Certificate newCertificate) throws UnrecoverableKeyException,
            KeyStoreException, NoSuchAlgorithmException {

        Key key = keystore.getKey(alias, password);
        PublicKey publicKey = newCertificate.getPublicKey();
        if (key instanceof PrivateKey) {
            KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) key);
            
            Certificate[] certChain = keystore.getCertificateChain(alias);
            int i = 0;
            certChain[i] = newCertificate;

            Principal signer = newCertificate.getIssuerDN();
            Principal requester = newCertificate.getSubjectDN();
            List<X509Certificate> certificates = getAllKeystoreCertificates();
            while (!signer.equals(requester)) {
                i++;
                requester = signer;
                boolean isCACertAvailable = false;

                for (X509Certificate certificate : certificates) {
                    if (requester.equals(certificate.getSubjectDN())) {
                        certChain[i] = certificate;
                        signer = certificate.getIssuerDN();
                        isCACertAvailable = true;
                        break;
                    }
                }

                if (!isCACertAvailable) {
                    CertificateTrackerLogger.LOGGER.untrustedRootCA(alias);
                }
            }

            keystore.setKeyEntry(alias, keyPair.getPrivate(), password, certChain);
        }
    }

    /**
     * Loads keystore from specified file
     * 
     * @param keystoreType
     * @param keystorePath
     * @param password
     * @return keystore
     */
    private KeyStore loadKeyStore(String keystoreType, String keystorePath, char[] password) {

        if (keystoreType.isEmpty()) {
            keystoreType = KeyStore.getDefaultType();
        }

        KeyStore keyStore = null;
        InputStream input = null;
        try {
            keyStore = KeyStore.getInstance(keystoreType);
            input = new FileInputStream(keystorePath);
            keyStore.load(input, password);
        } catch (Exception ex) {
            CertificateTrackerLogger.LOGGER.cannotLoadKeystore(ex);
            return null;
        } finally {
            try {
                input.close();
            } catch (Exception e) {
            }
        }
        
        return keyStore;
    }

    /**
     * Copies certificates from managed keystore to another (both keystore has
     * to be initialized.
     * 
     * @param toKeyStore
     * @throws KeyStoreException
     */
    private void copyCertificates(KeyStore toKeyStore) throws KeyStoreException {

        for (String alias : getKeystoreAliases()) {
            toKeyStore.setCertificateEntry(alias, keystore.getCertificate(alias));
        }
    }

}

