package org.jboss.certificate.tracker.core;

import java.io.File;
import java.io.FileOutputStream;
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
import java.util.List;

import org.jboss.logging.Logger;

public class KeystoreManager {

    private final Logger log = Logger.getLogger(KeystoreManager.class);

    private final String name;
    private final String keystorePath;
    private final String keystoreType;
    private final String password;
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
        this.keystoreType = keystoreType;
        this.password = password;
        this.managedAliases = managedAliases;
        this.isUpdated = false;

        keystore = KeyStoreUtils.loadKeyStore(keystoreType, keystorePath, password);

    }

    public String getName() {
        return name;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public KeyStore getTrustStore() {

        KeyStore trustStore = null;
        try {
            trustStore = KeyStoreUtils.createTrustStore();
            KeyStoreUtils.copyCertificates(keystore, trustStore);
        } catch (Exception ex) {
            log.error("Unable to create trustStore from keystore manager: " + name, ex);
        }
        return trustStore;
    }

    public String[] getCertAliases() {
        return KeyStoreUtils.getCertAliases(keystore);
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public X509Certificate getCertByAlias(String alias) throws KeyStoreException {

        if (keystore.getCertificate(alias).getType().equals("X.509")) {
            return (X509Certificate) keystore.getCertificate(alias);
        }
        return null;
    }

    public List<X509Certificate> getAllKeystoreCertificates() throws KeyStoreException {

        List<X509Certificate> keystoreCertificates = new ArrayList<X509Certificate>();
        for (String alias : getCertAliases()) {
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
                log.error("Certificate with alias: '" + managedAliases[i] + "' was not found in keystore: " + keystorePath);
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
                log.info("Certificate for KeyPair: " + alias + " has been updated in Keystore named: " + name);
            } else {
                log.debug("New certificate with SubjectDN: " + oldCertificate.getSubjectDN().getName()
                        + " is available, but has different KeyPair. To update please import new KeyPair.");
            }
        } else {
            keystore.setCertificateEntry(alias, newCertificate);
            isUpdated = true;
            log.info("Certificate with alias: " + alias + " has been updated in Keystore named: " + name);
        }
    }

    public void saveKeystore() {

        File keystoreFile = new File(keystorePath);
        try {
            keystore.store(new FileOutputStream(keystoreFile), password.toCharArray());
            isUpdated = false;
        } catch (Exception ex) {
            log.error("Unable to save keystore: " + keystorePath, ex);
        }

    }

    public void setKeyEntryWithCertificate(String alias, X509Certificate newCertificate) throws UnrecoverableKeyException,
            KeyStoreException, NoSuchAlgorithmException {

        Key key = keystore.getKey(alias, password.toCharArray());
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
                    log.error("Cannot establish trusted path to root CA");
                }
            }

            keystore.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(), certChain);
        }
    }

}

