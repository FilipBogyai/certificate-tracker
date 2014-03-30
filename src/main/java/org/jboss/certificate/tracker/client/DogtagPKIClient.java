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
import org.jboss.logging.Logger;

public class DogtagPKIClient implements PKIClient {

    private CertClient certClient = null;
    private final Logger log = Logger.getLogger(DogtagPKIClient.class);

    public DogtagPKIClient() {

    }

    public DogtagPKIClient(String urlTarget) throws URISyntaxException {

        certClient = new CertClient(urlTarget);
    }

    public DogtagPKIClient(String urlTarget, KeyStore trustStore) throws URISyntaxException {

        certClient = new CertClient(urlTarget, trustStore);
    }

    @Override
    public void init(String urlTarget, KeyStore trustStore) {
        try {
            certClient = trustStore == null ? new CertClient(urlTarget) : new CertClient(urlTarget, trustStore);

        } catch (URISyntaxException ex) {
            log.error("URL address of CA is wrong", ex);
        }


    }

    @Override
    public boolean isInitialized() {

        return certClient != null;
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
