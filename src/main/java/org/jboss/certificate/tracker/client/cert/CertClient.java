package org.jboss.certificate.tracker.client.cert;

import java.net.URISyntaxException;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class CertClient {

    private final String urlTarget;
    public CertResource certClient;

    public CertClient(String urlTarget) throws URISyntaxException {

        this.urlTarget = urlTarget;
        init();
    }

    public void init() throws URISyntaxException {

        ResteasyClient resteasyClient = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = resteasyClient.target(urlTarget);
        certClient = target.proxy(CertResource.class);
    }

    public CertData getCert(int id) {
        return getCert(new CertId(id));
    }

    public CertData getCert(CertId id) {
        return certClient.getCert(id);
    }

    public CertDataInfos listCerts() {
        return certClient.listCerts();
    }

    public CertDataInfos listCerts(String status, Integer maxResults, Integer maxTime, Integer start, Integer size) {
        return certClient.listCerts(status, maxResults, maxTime, start, size);
    }


    public static void main(String args[]) throws Exception {
    
        ResteasyClient certClient = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = certClient.target("http://vm-144.idm.lab.eng.brq.redhat.com:8080/ca/rest");
        CertResource service = target.proxy(CertResource.class);
        
        CertData data = service.getCert(new CertId(1));
        System.out.println(data.getEncoded());
    }

}
