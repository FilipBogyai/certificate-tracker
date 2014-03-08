package org.jboss.certificate.tracker.client.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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

    public KeyStore getKeystore() {

        return keystore;
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

        if (managedAliases[0].isEmpty() || managedAliases[0].equals("undefined")) {
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

    public void addCertificateToChain(X509Certificate certificate) {

    }

    public void saveKeystore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
            IOException {

        File keystoreFile = new File(keystorePath);
        keystore.store(new FileOutputStream(keystoreFile), password.toCharArray());

        isUpdated = false;
    }

    // FIXME
    public X509Certificate generateCertificate() throws GeneralSecurityException, IOException {

        for (String alias : getCertAliases()) {

            if (keystore.isKeyEntry(alias)) {
                Key key = keystore.getKey(alias, password.toCharArray());
                if (key instanceof PrivateKey) {
                    // Get certificate of public key
                    Certificate cert = keystore.getCertificate(alias);
                    String algorithm = ((X509Certificate) cert).getSigAlgName();
                    String dn = ((X509Certificate) cert).getSubjectDN().toString();
                    // Get public key
                    PublicKey publicKey = cert.getPublicKey();

                    // Return a key pair
                    KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) key);
                    X509Certificate certificate = null;
                    // CertificateUtils.generateCertificate(dn, keyPair, 100,
                    // algorithm);
                    Certificate[] cer = new Certificate[] { certificate };
                    keystore.setKeyEntry(alias, key, password.toCharArray(), cer);
                    keystore.setKeyEntry("blabla", key, password.toCharArray(), cer);
                    System.out.println(getCertByAlias(alias));
                    return certificate;
                }
            }
        }
        return null;
    }

    public void setKeyEntryWithCertificate(String alias, X509Certificate newCertificate) throws UnrecoverableKeyException,
            KeyStoreException, NoSuchAlgorithmException {

        Key key = keystore.getKey(alias, password.toCharArray());
        PublicKey publicKey = newCertificate.getPublicKey();
        if (key instanceof PrivateKey) {
            KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) key);
            Certificate[] certChain = new Certificate[1];
            certChain[0] = newCertificate;

            keystore.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(), certChain);
        }
    }

}

