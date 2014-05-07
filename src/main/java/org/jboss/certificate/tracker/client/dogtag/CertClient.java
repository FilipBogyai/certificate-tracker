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
package org.jboss.certificate.tracker.client.dogtag;

import java.net.URISyntaxException;
import java.security.KeyStore;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * Class representing REST client for Dogtag Certificate System
 * 
 * @author Filip Bogyai
 */
public class CertClient {

    private final CertResource certClient;

    /**
     * Constructor for CertClient, which creates REST client for defined URL target
     * with optional truststore containing trusted certificate
     * 
     * @param urlTarget address of root REST interface of Dogtag 
     * @param truststore with trusted certificate of Dogtag   
     */
    public CertClient(String urlTarget, KeyStore truststore) throws URISyntaxException {

        if (urlTarget == null) {
            throw new IllegalArgumentException("URL target for Dogtag REST client cannot be null");
        }

        ResteasyClient resteasyClient = null;

        if (truststore != null) {
            resteasyClient = new ResteasyClientBuilder().trustStore(truststore).build();
        } else {
            resteasyClient = new ResteasyClientBuilder().build();
        }

        ResteasyWebTarget target = resteasyClient.target(urlTarget);
        certClient = target.proxy(CertResource.class);

    }

    /**
     * Get certificate data with specified id
     * 
     * @param id number of requested certificate     
     */
    public CertData getCert(int id) {
        return getCert(new CertId(id));
    }

    /**
     * Get certificate data with specified id
     * 
     * @param id number of requested certificate     
     */
    public CertData getCert(CertId id) {
        Response response = certClient.getCert(id);
        return response.readEntity(CertData.class);
    }

    /**
     * Get all information about certificates depending on search criteria
     * 
     * @param status of certificate (VALID/REVOKED)
     * @param maxResults maximal number of returned certificates
     * @param maxTime end date of certificate validity
     * @param start id number of first returned certificate  
     * @param size number of requested certificates
     */
    public CertDataInfos listCerts(String status, Integer maxResults, Integer maxTime, Integer start, Integer size) {
        Response response = certClient.listCerts(status, maxResults, maxTime, start, size);
        return response.readEntity(CertDataInfos.class);
    }

}
