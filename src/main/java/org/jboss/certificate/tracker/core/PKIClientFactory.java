package org.jboss.certificate.tracker.core;

import org.jboss.certificate.tracker.extension.CertificateTrackerLogger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;

public class PKIClientFactory {

    public static PKIClient get(ClassLoader classLoader, String name){
        
        Class<?> loadedClass = null;        
        PKIClient pkiClient = null;
        try {
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

    public static PKIClient get(String module, String name) {

        PKIClient pkiClient = null;

        try {
            ModuleLoader loader = Module.getCallerModuleLoader();
            Module customModule = loader.loadModule(ModuleIdentifier.fromString(module));
            ClassLoader classLoader = customModule.getClassLoader();
            pkiClient = get(classLoader, name);
        } catch (ModuleLoadException ex) {
            CertificateTrackerLogger.LOGGER.moduleNotFound(ex);
        }         
        return pkiClient;
    }
}
