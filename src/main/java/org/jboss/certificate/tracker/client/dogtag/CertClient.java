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


public class CertClient {

    private final String urlTarget;
    private final KeyStore truststore;
    public CertResource certClient;

    public CertClient(String urlTarget) throws URISyntaxException {

        this.urlTarget = urlTarget;
        this.truststore = null;
        init();
    }
    
    public CertClient(String urlTarget, KeyStore truststore) throws URISyntaxException {

        this.urlTarget = urlTarget;
        this.truststore = truststore;
        init();
    }

    public void init() throws URISyntaxException {

        ResteasyClient resteasyClient = null;
        if (truststore != null) {
            resteasyClient = new ResteasyClientBuilder().trustStore(truststore).build();
        } else {
            resteasyClient = new ResteasyClientBuilder().build();
        }

        ResteasyWebTarget target = resteasyClient.target(urlTarget);
        certClient = target.proxy(CertResource.class);
    }

    public CertData getCert(int id) {
        return getCert(new CertId(id));
    }

    public CertData getCert(CertId id) {
        Response response = certClient.getCert(id);
        return response.readEntity(CertData.class);
    }

    public CertDataInfos listCerts(String status, Integer maxResults, Integer maxTime, Integer start, Integer size) {
        Response response = certClient.listCerts(status, maxResults, maxTime, start, size);
        return response.readEntity(CertDataInfos.class);
    }

    public static void main(String args[]) throws Exception {
    
        CertClient service = new CertClient("http://vm-144.idm.lab.eng.brq.redhat.com:8080/ca/rest");
        
        CertData data = service.getCert(new CertId(1));
        System.out.println(data);
    }

}
