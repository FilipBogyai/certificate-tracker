package org.jboss.certificate.tracker.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;

/**
 * @author Filip Bogyai
 */
public class CertificateTrackerDefinition extends SimpleResourceDefinition {
    public static final CertificateTrackerDefinition INSTANCE = new CertificateTrackerDefinition();

    private CertificateTrackerDefinition() {
        super(CertificateTrackerExtension.SUBSYSTEM_PATH,
                CertificateTrackerExtension.getResourceDescriptionResolver(null),
                //We always need to add an 'add' operation
                CertificateTrackerAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                CertificateTrackerRemove.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        //you can register aditional operations here
        resourceRegistration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE,
                GenericSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
    }

}
