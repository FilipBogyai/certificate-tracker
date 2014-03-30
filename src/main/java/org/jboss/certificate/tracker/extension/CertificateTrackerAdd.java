package org.jboss.certificate.tracker.extension;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
 * Handler responsible for adding the certificate tracker subsystem resource to
 * the model
 * 
 * @author Filip Bogyai
 */
class CertificateTrackerAdd extends AbstractBoottimeAddStepHandler {

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
