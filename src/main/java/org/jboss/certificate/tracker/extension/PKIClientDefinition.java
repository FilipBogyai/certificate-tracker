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
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.TIME_INTERVAL, ModelType.LONG)
            .setAllowExpression(true)
            .setXmlName(CertificateTrackerExtension.TIME_INTERVAL)
            .setDefaultValue(new ModelNode(DEFAULT_TIME_INTERVAL))
            .setAllowNull(true)
            .build();
    
    protected static final SimpleAttributeDefinition TRUSTSTORE_NAME =
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.TRUSTSTORE_NAME, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(CertificateTrackerExtension.TRUSTSTORE_NAME)            
            .setAllowNull(true)
            .build();
    
    protected static final SimpleAttributeDefinition URL = 
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.URL, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(CertificateTrackerExtension.URL)
            .setAllowNull(false)
            .build();
    
    protected static final SimpleAttributeDefinition MODULE = 
            new SimpleAttributeDefinitionBuilder(CertificateTrackerExtension.MODULE, ModelType.STRING)
            .setAllowExpression(true)
            .setXmlName(CertificateTrackerExtension.MODULE)            
            .setAllowNull(true)            
            .build();
    
    public static AttributeDefinition[] ALL_ATTRIBUTES = new AttributeDefinition[] { TIME_INTERVAL, TRUSTSTORE_NAME, URL, MODULE };

    private PKIClientDefinition() {
        super(CertificateTrackerExtension.PKI_CLIENT_PATH, CertificateTrackerExtension.getResourceDescriptionResolver(CertificateTrackerExtension.PKI_CLIENT),
                PKIClientAddHandler.INSTANCE, PKIClientRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        // write attribute for time_interval
        resourceRegistration.registerReadWriteAttribute(TIME_INTERVAL, null, PKIClientTimeIntervalHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(TRUSTSTORE_NAME, null, new ReloadRequiredWriteAttributeHandler(TRUSTSTORE_NAME));
        resourceRegistration.registerReadWriteAttribute(URL, null, new ReloadRequiredWriteAttributeHandler(URL));
        resourceRegistration.registerReadWriteAttribute(MODULE, null, new ReloadRequiredWriteAttributeHandler(MODULE));
    }

}
