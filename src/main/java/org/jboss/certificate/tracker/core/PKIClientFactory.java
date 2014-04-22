package org.jboss.certificate.tracker.core;

import org.jboss.logging.Logger;

public class PKIClientFactory {

    private static final Logger log = Logger.getLogger(PKIClientFactory.class);

    public static PKIClient get(ClassLoader classLoader, String name){
        
        Class<?> loadedClass = null;
        PKIClient pkiClient = null;

        try
        {
           loadedClass = classLoader.loadClass(name);
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
