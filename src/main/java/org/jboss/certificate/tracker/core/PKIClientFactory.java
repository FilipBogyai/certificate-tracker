package org.jboss.certificate.tracker.core;

import org.jboss.certificate.tracker.client.DogtagPKIClient;
import org.jboss.logging.Logger;

public class PKIClientFactory {

    private static final Logger log = Logger.getLogger(PKIClientFactory.class);

    public static PKIClient get() {
        return new DogtagPKIClient();
    }

    public static PKIClient get(ClassLoader classLoader, String code){
        
        Class<?> loadedClass = null;
        PKIClient pkiClient = null;

        try
        {
           loadedClass = classLoader.loadClass(code);
        } catch (ClassNotFoundException e) {
            log.error("Unable to find PKIClient class", e);
        }
        
        try {
            pkiClient = (PKIClient) loadedClass.newInstance();
        } catch (Exception e) {
            log.error("Unable to create new PKIClient", e);
        }
        return pkiClient;
    }

}
