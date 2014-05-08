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
package org.jboss.certificate.tracker.core;

import org.jboss.certificate.tracker.extension.CertificateTrackerLogger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;

/**
 * This class is used for creating new instance of {@link PKIClient} implementation, 
 * which is loaded by classloader from defined class/module.
 * 
 * @author Filip Bogyai
 */
public class PKIClientFactory {

    
    /**
     * Creates new instance of defined {@link PKIClient} class, 
     * which is loaded by provided classloader.
     * 
     * @param classLoader to use for loading class
     * @param name fully qualified name of class to load
     */
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

    /**
     * Creates new instance of defined {@link PKIClient} class, 
     * which is loaded from provided module.
     * 
     * @param module where the class is 
     * @param name fully qualified name of class to load
     */
    public static PKIClient getFromModule(String module, String name) {

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
