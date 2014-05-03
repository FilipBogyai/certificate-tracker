package org.jboss.certificate.tracker.extension;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.certificate.tracker.core.KeystoresTrackingManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class CertificateTrackingService implements Service<CertificateTrackingService> {

    private long timeInterval;
    private final String name;
    private final String module;
    private final Map<String, Object> options;
    private Timer timer = null;

    public CertificateTrackingService(String name, long timeInterval, String module, Map<String, Object> options) {

        this.name = name;
        this.timeInterval = timeInterval;
        this.module = module;
        this.options = options;

    }

    @Override
    public CertificateTrackingService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext arg0) throws StartException {

        KeystoresTrackingManager.INSTANCE.setName(name);
        KeystoresTrackingManager.INSTANCE.setModule(module);
        KeystoresTrackingManager.INSTANCE.setOptions(options);

        if (timeInterval > 0) {
            if (timer == null) {
                timer = new Timer();
            }
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    try {
                        CertificateTrackerLogger.LOGGER.checkingCertificates();
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

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

}
