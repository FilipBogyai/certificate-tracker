package org.jboss.certificate.tracker.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jboss.certificate.tracker.client.dogtag.CertClient;
import org.jboss.certificate.tracker.client.dogtag.CertData;
import org.jboss.certificate.tracker.client.dogtag.CertDataInfo;
import org.jboss.certificate.tracker.client.dogtag.CertDataInfos;
import org.jboss.certificate.tracker.core.CertificateInfo;
import org.jboss.certificate.tracker.core.KeystoresTrackingManager;
import org.jboss.certificate.tracker.core.PKIClient;
import org.jboss.certificate.tracker.extension.CertificateTrackerLogger;

public class DogtagPKIClient implements PKIClient {

    public static final String DOGTAG = "Dogtag";
    public static final String URL = "url";
    public static final String TRUSTSTORE_NAME = "truststore-name";
    private CertClient certClient = null;

    public DogtagPKIClient() {

    }

    public DogtagPKIClient(String urlTarget) throws URISyntaxException {

        certClient = new CertClient(urlTarget);
    }

    public DogtagPKIClient(String urlTarget, KeyStore trustStore) throws URISyntaxException {

        certClient = new CertClient(urlTarget, trustStore);
    }

    @Override
    public void init(Map<String, Object> options) {
        try {

            String url = (String) options.get(URL);
            String trustStoreName = (String) options.get(TRUSTSTORE_NAME);
            KeyStore trustStore = KeystoresTrackingManager.INSTANCE.getTrustStore(trustStoreName);

            certClient = trustStore == null ? new CertClient(url) : new CertClient(url, trustStore);

        } catch (URISyntaxException ex) {
            CertificateTrackerLogger.LOGGER.invalidURL(ex);
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
        // load certificate from binary representation
        ByteArrayInputStream binStream = null;
        X509Certificate certificate = null;
        try {
            final CertificateFactory certFac = CertificateFactory.getInstance("X.509");
            binStream = new ByteArrayInputStream(binaryCertificate);
            certificate = (X509Certificate) certFac.generateCertificate(binStream);
        } catch (Exception ex) {
            CertificateTrackerLogger.LOGGER.cannotLoadBinaryCertificate(ex);
        } finally {
            try {
                binStream.close();
            } catch (IOException ex) {
                // OK
            }
        }

        return certificate;
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
