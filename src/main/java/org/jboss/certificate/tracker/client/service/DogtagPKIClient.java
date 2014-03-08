package org.jboss.certificate.tracker.client.service;

import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.certificate.tracker.client.cert.CertClient;
import org.jboss.certificate.tracker.client.cert.CertData;
import org.jboss.certificate.tracker.client.cert.CertDataInfo;
import org.jboss.certificate.tracker.client.cert.CertDataInfos;

public class DogtagPKIClient implements PKIClient {

    CertClient certClient;

    public DogtagPKIClient(String urlTarget) throws URISyntaxException {

        certClient = new CertClient(urlTarget);
    }

    public DogtagPKIClient(String urlTarget, String trustStorePath, String password, String keystoreType) throws URISyntaxException {

        KeyStore trustStore = KeyStoreUtils.loadKeyStore(keystoreType, trustStorePath, password);
        certClient = new CertClient(urlTarget, trustStore);
    }

    public DogtagPKIClient(String urlTarget, KeyStore trustStore) throws URISyntaxException {

        certClient = new CertClient(urlTarget, trustStore);
    }

    @Override
    public X509Certificate getCert(String id) {

        CertData certData = certClient.getCert(Integer.parseInt(id));
        byte[] binaryCertificate = certData.getEncoded().getBytes();

        return KeyStoreUtils.loadBinaryCertificate(binaryCertificate);
    }

    @Override
    public Collection<CertificateInfo> listCerts() {

        CertDataInfos certDataInfos = certClient.listCerts(null, null, null, null, null);
        Collection<CertificateInfo> certificateInfos = new ArrayList<CertificateInfo>();
        for (CertDataInfo certDataInfo : certDataInfos.getEntries()) {

            CertificateInfo keystoreCertificate = new CertificateInfo(
                    certDataInfo.getID().toString(),
                    certDataInfo.getSubjectDN(),
                    certDataInfo.getStatus(),
                    certDataInfo.getType(),
                    certDataInfo.getVersion(), 
                    certDataInfo.getNotValidBefore(),
                    certDataInfo.getNotValidAfter(),                    
                    certDataInfo.getIssuedOn(),
                    certDataInfo.getIssuedBy());

            certificateInfos.add(keystoreCertificate);
        }
        return certificateInfos;
    }

}
