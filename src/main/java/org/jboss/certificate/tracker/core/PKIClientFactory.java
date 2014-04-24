package org.jboss.certificate.tracker.core;

import org.jboss.certificate.tracker.extension.CertificateTrackerLogger;

public class PKIClientFactory {

    public static PKIClient get(ClassLoader classLoader, String name){
        
        Class<?> loadedClass = null;
        PKIClient pkiClient = null;

        try
        {
           loadedClass = classLoader.loadClass(name);
        } catch (ClassNotFoundException ex) {
            CertificateTrackerLogger.LOGGER.unableToFindClass(ex);
        }
        
        try {
            pkiClient = (PKIClient) loadedClass.newInstance();
        } catch (Exception ex) {
            CertificateTrackerLogger.LOGGER.unableToCreateClass(ex);
        }
        return pkiClient;
    }

}
