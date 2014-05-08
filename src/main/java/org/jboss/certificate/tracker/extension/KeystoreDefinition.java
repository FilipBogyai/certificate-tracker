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

import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Definition of keystore element with possible attributes
 * 
 * @author Filip Bogyai
 */
public class KeystoreDefinition extends SimpleResourceDefinition {

    public static final KeystoreDefinition INSTANCE = new KeystoreDefinition();

    protected static final SimpleAttributeDefinition PATH = 
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.PATH,
            ModelType.STRING).
            setAllowExpression(true).
            setXmlName(CertificateTrackerExtension.PATH).
            setAllowNull(false).
            build();
    
    protected static final SimpleAttributeDefinition PASSWORD = 
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.PASSWORD,
            ModelType.STRING).
            setAllowExpression(true).
            setXmlName(CertificateTrackerExtension.PASSWORD).
            setAllowNull(false).
            build();

    protected static final SimpleAttributeDefinition TYPE = 
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.TYPE, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(CertificateTrackerExtension.TYPE)
            .setDefaultValue(new ModelNode("JKS"))
            .setAllowNull(true)
            .build();
    
    protected static final SimpleAttributeDefinition ALIASES = 
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.ALIASES, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(CertificateTrackerExtension.ALIASES)
            .setAllowNull(true)            
            .build();

    private KeystoreDefinition() {
        super(CertificateTrackerExtension.KEYSTORE_PATH, CertificateTrackerExtension.getResourceDescriptionResolver(CertificateTrackerExtension.KEYSTORE),
                KeystoreAddHandler.INSTANCE, KeystoreRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {

        resourceRegistration.registerReadWriteAttribute(PATH, null, new ReloadRequiredWriteAttributeHandler(PATH));
        resourceRegistration.registerReadWriteAttribute(TYPE, null, new ReloadRequiredWriteAttributeHandler(TYPE));
        resourceRegistration.registerReadWriteAttribute(PASSWORD, null, new ReloadRequiredWriteAttributeHandler(PASSWORD));
        resourceRegistration.registerReadWriteAttribute(ALIASES, null, new ReloadRequiredWriteAttributeHandler(ALIASES));
    }

}
