package org.jboss.certificate.tracker.extension;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
 * Root definition of certificate tracker subsystem
 * 
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

    /**
     * Handler responsible for adding the certificate tracker subsystem resource
     * to the model
     */
    static class CertificateTrackerAdd extends AbstractBoottimeAddStepHandler {

        static final CertificateTrackerAdd INSTANCE = new CertificateTrackerAdd();

        private CertificateTrackerAdd() {
        }

        @Override
        protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
                ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {

        }

        @Override
        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

        }

    }

    /**
     * Handler responsible for removing the certificate tracker subsystem
     * resource from the model
     */
    static class CertificateTrackerRemove extends AbstractRemoveStepHandler {

        static final CertificateTrackerRemove INSTANCE = new CertificateTrackerRemove();

        private CertificateTrackerRemove() {
        }

    }

}
