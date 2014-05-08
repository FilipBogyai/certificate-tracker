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

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.MapAttributeDefinition;
import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.AttributeAccess.Flag;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Definition of pki-client element with possible attributes
 * 
 * @author Filip Bogyai
 */
public class PKIClientDefinition extends SimpleResourceDefinition {

    public static final PKIClientDefinition INSTANCE = new PKIClientDefinition();
    
    public static final int DEFAULT_TIME_INTERVAL = 60000;

    protected static final SimpleAttributeDefinition TIME_INTERVAL =
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.TIME_INTERVAL, ModelType.LONG)
            .setAllowExpression(true)
            .setXmlName(CertificateTrackerExtension.TIME_INTERVAL)
            .setDefaultValue(new ModelNode(DEFAULT_TIME_INTERVAL))
            .setAllowNull(true)
            .build();
    
    protected static final SimpleAttributeDefinition MODULE = 
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.MODULE, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(CertificateTrackerExtension.MODULE)            
            .setAllowNull(true)            
            .build();
    
    protected static final PropertiesAttributeDefinition CLIENT_OPTIONS = new PropertiesAttributeDefinition.Builder(CertificateTrackerExtension.CLIENT_OPTIONS, true)
            .addFlag(Flag.RESTART_ALL_SERVICES)
            .setAllowExpression(true)
            .setCorrector(MapAttributeDefinition.LIST_TO_MAP_CORRECTOR)
            .setValidator(new StringLengthValidator(1, true, true))
            .build();
    
    public static AttributeDefinition[] ALL_ATTRIBUTES = new AttributeDefinition[] { TIME_INTERVAL, MODULE, CLIENT_OPTIONS };

    private PKIClientDefinition() {
        super(CertificateTrackerExtension.PKI_CLIENT_PATH, CertificateTrackerExtension.getResourceDescriptionResolver(CertificateTrackerExtension.PKI_CLIENT),
                PKIClientAddHandler.INSTANCE, PKIClientRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {

        resourceRegistration.registerReadWriteAttribute(TIME_INTERVAL, null, PKIClientTimeIntervalHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(CLIENT_OPTIONS, null, new ReloadRequiredWriteAttributeHandler(CLIENT_OPTIONS));       
        resourceRegistration.registerReadWriteAttribute(MODULE, null, new ReloadRequiredWriteAttributeHandler(MODULE));
    }

}
