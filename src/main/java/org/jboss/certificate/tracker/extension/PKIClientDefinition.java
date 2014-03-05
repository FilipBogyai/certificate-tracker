package org.jboss.certificate.tracker.extension;

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
    
    private PKIClientDefinition() {
        super(SubsystemExtension.PKI_CLIENT_PATH, SubsystemExtension.getResourceDescriptionResolver(SubsystemExtension.PKI_CLIENT),
                PKIClientAddHandler.INSTANCE, PKIClientRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        // write attribute for time_interval
        resourceRegistration.registerReadWriteAttribute(TIME_INTERVAL, null, PKIClientTimeIntervalHandler.INSTANCE);
    }

}
