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

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;

public interface PKIClient {

    /**
     * Initialize the PKI client with customizable options
     * 
     * @param map of options key/value    
     */
    void init(Map<String, Object> options);

    /**
     * Determine if the PKI client is initialized.
     * 
     * @return boolean if the client is initialized
     */
    boolean isInitialized();

    /**
     * Get certificate with specified id.
     * 
     * @param id number of certificate
     * @return X509Certificate
     */
    public X509Certificate getCertificate(String id);

    /**
     * Get information about all available certificates
     * 
     * @return Collection<{@link CertificateInfo}> of information about all available certificates.
     */
    public Collection<CertificateInfo> listCertificates();

}
