package org.jboss.certificate.tracker.extension;

import java.util.Timer;
import java.util.TimerTask;

import org.jboss.certificate.tracker.core.KeystoresTrackingManager;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class CertificateTrackingService implements Service<CertificateTrackingService> {

    private final Logger log = Logger.getLogger(CertificateTrackingService.class);

    private String url;
    private String trustStoreName;
    private long timeInterval = 600000;
    private Timer timer = null;

    public CertificateTrackingService(String url, long timeInterval) {
        
        this.url = url;
        this.timeInterval = timeInterval;
    }

    public CertificateTrackingService(String url, String trustStoreName, long timeInterval) {

        this.url = url;
        this.trustStoreName = trustStoreName;
        this.timeInterval = timeInterval;
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
                    log.info("Checking certificates entires ");
                    KeystoresTrackingManager.INSTANCE.setUrlTarget(url);
                    KeystoresTrackingManager.INSTANCE.setTrustStoreManagerName(trustStoreName);
                    try {
                        KeystoresTrackingManager.INSTANCE.updateAllKeystores();
                    } catch (Exception ex) {
                        log.error(ex);
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
