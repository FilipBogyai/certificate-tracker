package org.jboss.certificate.tracker.core;

import java.security.cert.X509Certificate;
import java.util.Collection;

public interface PKIClient {

    /**
     * Get certificate with specified id
     * 
     * @param id
     */
    public X509Certificate getCertificate(String id);

    /**
     * Get information about all available certificates
     * 
     */
    public Collection<CertificateInfo> listCertificates();

}
