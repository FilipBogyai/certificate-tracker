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

/**
 * This class is responsible for management of one java keystore. 
 * It supports keystore types: "JKS","JCEKS","PKCS12".
 * 
 * @author Filip Bogyai
 */
public class JavaKeystoreManager implements KeystoreManager {

    private final String name;
    private final String keystorePath;
    private final char[] password;
    private final KeyStore keystore;
    private final String[] managedAliases;
    private boolean changed;

    /**
     * Constructor for JavaKeystoreManager
     * 
     * @param name for this keystore manager
     * @param keystorePath file path of keystore
     * @param keystoreType type of keystore
     * @param password of keystore
     */
    public JavaKeystoreManager(String name, String keystorePath, String keystoreType, String password) {

        this(name, keystorePath, keystoreType, password, "");
    }

    /**
     * Constructor for JavaKeystoreManager
     * 
     * @param name of keystore manager
     * @param keystorePath file path of keystore
     * @param keystoreType type of keystore
     * @param password of keystore
     * @param aliases comma separated names of certificate aliases which should be managed
     */
    public JavaKeystoreManager(String name, String keystorePath, String keystoreType, String password, String aliases) {

        this(name, keystorePath, keystoreType, password, aliases.split(","));
    }

    /**
     * Constructor for JavaKeystoreManager
     * 
     * @param name of keystore manager
     * @param keystorePath file path of keystore
     * @param keystoreType type of keystore
     * @param password of keystore
     * @param managedAliases array of certificate aliases which should be managed
     */
    public JavaKeystoreManager(String name, String keystorePath, String keystoreType, String password, String[] managedAliases) {

        this.name = name;
        this.keystorePath = keystorePath;
        this.password = password.toCharArray();
        this.managedAliases = managedAliases;
        this.changed = false;

        keystore = loadKeyStore(keystoreType, keystorePath, this.password);

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getKeystorePath() {
        return keystorePath;
    }
    
    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
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

    @Override
    public String[] getKeystoreAliases() {

        List<String> aliases = new ArrayList<String>();
        try {
            Enumeration<String> aliasEnum = keystore.aliases();
            while (aliasEnum.hasMoreElements()) {
                aliases.add(aliasEnum.nextElement());
            }
        } catch (KeyStoreException ex) {
            CertificateTrackerLogger.LOGGER.cannotLoadAliases(ex);
        }

        return aliases.toArray(new String[aliases.size()]);
    }

    @Override
    public X509Certificate getCertByAlias(String alias) {

        X509Certificate certificate = null;
        try {
            if (keystore.getCertificate(alias).getType().equals("X.509")) {
                certificate = (X509Certificate) keystore.getCertificate(alias);
            }
        } catch (NullPointerException ex) {
            CertificateTrackerLogger.LOGGER.noCertificateWithAlias(alias, name);
        } catch (KeyStoreException ex) {
            CertificateTrackerLogger.LOGGER.cannotLoadCertificate(alias, name, ex);
        }


        return certificate;
    }

    @Override
    public List<X509Certificate> getAllCertificates() {

        List<X509Certificate> keystoreCertificates = new ArrayList<X509Certificate>();

        for (String alias : getKeystoreAliases()) {
            X509Certificate certificate = getCertByAlias(alias);
            if (certificate != null) {
                keystoreCertificates.add(certificate);
            }
        }

        return keystoreCertificates;

    }

    @Override
    public List<X509Certificate> getManagedCertificates() {

        if (managedAliases == null || managedAliases[0].isEmpty()) {
            return getAllCertificates();
        }

        List<X509Certificate> keystoreCertificates = new ArrayList<X509Certificate>();

        for (int i = 0; i < managedAliases.length; i++) {
            X509Certificate certificate = getCertByAlias(managedAliases[i]);
            if (certificate != null) {
                keystoreCertificates.add(certificate);
            }
        }

        return keystoreCertificates;

    }

    @Override
    public void replaceCertificate(X509Certificate oldCertificate, X509Certificate newCertificate) {
        
        try{
            String alias = keystore.getCertificateAlias(oldCertificate);
            
            if (keystore.isKeyEntry(alias)) {
    
                if (oldCertificate.getPublicKey().equals(newCertificate.getPublicKey())) {
                    setKeyEntryWithCertificate(alias, newCertificate);
                    changed = true;
                    CertificateTrackerLogger.LOGGER.updatedKeyCertificate(alias, name);
                } else {
                    CertificateTrackerLogger.LOGGER.differentKeyCertificate(oldCertificate.getSubjectDN().getName());
                }
            } else {
                keystore.setCertificateEntry(alias, newCertificate);
                changed = true;
                CertificateTrackerLogger.LOGGER.updatedCertificate(alias, name);
            }
            
        } catch (Exception ex) {
            CertificateTrackerLogger.LOGGER.unableToUpdateCertificate(oldCertificate.getSubjectDN().getName(), name, ex);
        }
    }

    @Override
    public void saveKeystore() {

        File keystoreFile = new File(keystorePath);
        try {
            keystore.store(new FileOutputStream(keystoreFile), password);
            changed = false;
        } catch (Exception ex) {
            CertificateTrackerLogger.LOGGER.unableToSaveKeystore(name, ex);
        }

    }

    /**
     * Replace old certificate of a keypair in keystore with new certificate
     * 
     * @param alias of keypair for which certificate will be changed
     * @param newCertificate as a replacement
     * 
     */
    private void setKeyEntryWithCertificate(String alias, X509Certificate newCertificate) throws UnrecoverableKeyException,
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
            List<X509Certificate> certificates = getAllCertificates();
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
     * Loads java keystore from specified file
     * 
     * @param keystoreType type of keystore
     * @param keystorePath file path
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
     * Copies certificates from managed keystore to provided keystore
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

