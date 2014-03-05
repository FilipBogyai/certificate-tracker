package org.jboss.certificate.tracker.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.certificate.tracker.client.service.KeystoresTrackingManager;
import org.jboss.dmr.ModelNode;

public class KeystoreRemoveHandler extends AbstractRemoveStepHandler{

    public static final KeystoreRemoveHandler INSTANCE = new KeystoreRemoveHandler();

    private KeystoreRemoveHandler() {

    }

    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model)
            throws OperationFailedException {

        // remove keystore manager from service
        String path = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        KeystoresTrackingManager.INSTANCE.removeKeystoreManager(path);
    }

}

