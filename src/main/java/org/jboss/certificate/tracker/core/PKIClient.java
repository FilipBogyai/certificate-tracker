package org.jboss.certificate.tracker.core;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;

public interface PKIClient {

    /**
     * Initialize the PKI client
     * 
     * @param map of options key/value    
     */
    void init(Map<String, Object> options);

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
