package org.jboss.certificate.tracker.core;

import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collection;

public interface PKIClient {

    /**
     * Initialize the PKI client
     * 
     * @param url
     * @param trustStore
     */
    void init(String url, KeyStore trustStore) throws URISyntaxException;

    /**
     * Determine if the PKI client is initialized
     * 
     * @return boolean
     */
    boolean isInitialized();

    /**
     * Get certificate with specified id
     * 
     * @param id
     * @return X509Certificate
     */
    public X509Certificate getCertificate(String id);

    /**
     * Get information about all available certificates
     * 
     * @return Collection<CertificateInfo>
     */
    public Collection<CertificateInfo> listCertificates();

}
