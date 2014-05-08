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

/**
 * Certificate tracking service, which periodically synchronize certificates
 * with PKI client by calling {@link KeystoresTrackingManager}.
 * 
 * @author Filip Bogyai
 */
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

        KeystoresTrackingManager.INSTANCE.setClientName(name);
        KeystoresTrackingManager.INSTANCE.setClientModule(module);
        KeystoresTrackingManager.INSTANCE.setClientOptions(options);

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
