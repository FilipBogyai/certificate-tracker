package org.jboss.certificate.tracker.extension;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class PKIClientDefinition extends SimpleResourceDefinition {

    public static final PKIClientDefinition INSTANCE = new PKIClientDefinition();
    
    public static final int DEFAULT_TIME_INTERVAL = 60000;

    protected static final SimpleAttributeDefinition TIME_INTERVAL =
            new SimpleAttributeDefinitionBuilder(SubsystemExtension.TIME_INTERVAL, ModelType.LONG)
            .setAllowExpression(true)
            .setXmlName(SubsystemExtension.TIME_INTERVAL)
            .setDefaultValue(new ModelNode(DEFAULT_TIME_INTERVAL))
            .setAllowNull(true)
            .build();
    
    protected static final SimpleAttributeDefinition TRUSTSTORE_NAME =
            new SimpleAttributeDefinitionBuilder(SubsystemExtension.TRUSTSTORE_NAME, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(SubsystemExtension.TRUSTSTORE_NAME)            
            .setAllowNull(true)
            .build();
    
    protected static final SimpleAttributeDefinition CODE = 
            new SimpleAttributeDefinitionBuilder(SubsystemExtension.CODE, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(SubsystemExtension.CODE)            
            .setAllowNull(true)            
            .build();
    
    protected static final SimpleAttributeDefinition MODULE = 
            new SimpleAttributeDefinitionBuilder(SubsystemExtension.MODULE, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(SubsystemExtension.MODULE)            
            .setAllowNull(true)
            .setRequires(CODE.getName())
            .build();
    
    public static AttributeDefinition[] ALL_ATTRIBUTES = new AttributeDefinition[] { TIME_INTERVAL, TRUSTSTORE_NAME, CODE, MODULE };

    private PKIClientDefinition() {
        super(SubsystemExtension.PKI_CLIENT_PATH, SubsystemExtension.getResourceDescriptionResolver(SubsystemExtension.PKI_CLIENT),
                PKIClientAddHandler.INSTANCE, PKIClientRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        // write attribute for time_interval
        resourceRegistration.registerReadWriteAttribute(TIME_INTERVAL, null, PKIClientTimeIntervalHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(TRUSTSTORE_NAME, null, new ReloadRequiredWriteAttributeHandler(TRUSTSTORE_NAME));
        resourceRegistration.registerReadWriteAttribute(CODE, null, new ReloadRequiredWriteAttributeHandler(CODE));
        resourceRegistration.registerReadWriteAttribute(MODULE, null, new ReloadRequiredWriteAttributeHandler(MODULE));
    }

}
