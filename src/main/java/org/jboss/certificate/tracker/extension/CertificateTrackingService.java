package org.jboss.certificate.tracker.extension;

import java.util.Timer;
import java.util.TimerTask;

import org.jboss.certificate.tracker.core.KeystoresTrackingManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class CertificateTrackingService implements Service<CertificateTrackingService> {

    private String url;
    private final String trustStoreName;
    private long timeInterval;
    private final String name;
    private final String module;
    private Timer timer = null;

    public CertificateTrackingService(String url, String trustStoreName, long timeInterval, String name, String module) {

        this.url = url;
        this.trustStoreName = trustStoreName;
        this.timeInterval = timeInterval;
        this.name = name;
        this.module = module;
    }

    @Override
    public CertificateTrackingService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext arg0) throws StartException {

        if (timeInterval > 0) {
            if (timer == null) {
                timer = new Timer();
            }
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    CertificateTrackerLogger.LOGGER.checkingCertificates();
                    KeystoresTrackingManager.INSTANCE.setUrlTarget(url);
                    KeystoresTrackingManager.INSTANCE.setTrustStoreManagerName(trustStoreName);
                    KeystoresTrackingManager.INSTANCE.setName(name);
                    KeystoresTrackingManager.INSTANCE.setModule(module);
                    try {
                        KeystoresTrackingManager.INSTANCE.updateAllKeystores();
                    } catch (Exception ex) {
                        CertificateTrackerLogger.LOGGER.checkingCertificatesError(ex);
                    }
                }
            }, timeInterval, timeInterval);
        }
    }

    @Override
    public void stop(StopContext arg0) {

        timer.cancel();
    }

    public static ServiceName getServiceName() {
        return ServiceName.JBOSS.append("certificate-tracker");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

}
