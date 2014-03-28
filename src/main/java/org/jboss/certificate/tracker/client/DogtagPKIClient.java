package org.jboss.certificate.tracker.client;

import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.certificate.tracker.client.dogtag.CertClient;
import org.jboss.certificate.tracker.client.dogtag.CertData;
import org.jboss.certificate.tracker.client.dogtag.CertDataInfo;
import org.jboss.certificate.tracker.client.dogtag.CertDataInfos;
import org.jboss.certificate.tracker.core.CertificateInfo;
import org.jboss.certificate.tracker.core.KeyStoreUtils;
import org.jboss.certificate.tracker.core.PKIClient;

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
    public X509Certificate getCertificate(String id) {

        CertData certData = certClient.getCert(Integer.parseInt(id));
        byte[] binaryCertificate = certData.getEncoded().getBytes();

        return KeyStoreUtils.loadBinaryCertificate(binaryCertificate);
    }

    @Override
    public Collection<CertificateInfo> listCertificates() {

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
