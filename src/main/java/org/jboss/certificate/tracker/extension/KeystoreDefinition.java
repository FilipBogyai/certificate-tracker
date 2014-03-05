package org.jboss.certificate.tracker.extension;

import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class KeystoreDefinition extends SimpleResourceDefinition {

    public static final KeystoreDefinition INSTANCE = new KeystoreDefinition();

    protected static final SimpleAttributeDefinition PASSWORD = 
            new SimpleAttributeDefinitionBuilder(SubsystemExtension.PASSWORD,
            ModelType.STRING).
            setAllowExpression(true).
            setXmlName(SubsystemExtension.PASSWORD).
            setAllowNull(false).
            build();

    protected static final SimpleAttributeDefinition TYPE = 
            new SimpleAttributeDefinitionBuilder(SubsystemExtension.TYPE, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(SubsystemExtension.TYPE)
            .setDefaultValue(new ModelNode("JKS"))
            .setAllowNull(true)
            .build();
    
    protected static final SimpleAttributeDefinition ALIASES = 
            new SimpleAttributeDefinitionBuilder(SubsystemExtension.ALIASES, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(SubsystemExtension.ALIASES)
            .setAllowNull(true)            
            .build();

    private KeystoreDefinition() {
        super(SubsystemExtension.KEYSTORE_PATH, SubsystemExtension.getResourceDescriptionResolver(SubsystemExtension.KEYSTORE),
                KeystoreAddHandler.INSTANCE, KeystoreRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        // write handlers

        // KeystoreTypeHandler.INSTANCE KeystorePasswordHandler.INSTANCE
        resourceRegistration.registerReadWriteAttribute(TYPE, null, new ReloadRequiredWriteAttributeHandler(TYPE));
        resourceRegistration.registerReadWriteAttribute(PASSWORD, null, new ReloadRequiredWriteAttributeHandler(PASSWORD));
        resourceRegistration.registerReadWriteAttribute(ALIASES, null, new ReloadRequiredWriteAttributeHandler(ALIASES));
    }

}
