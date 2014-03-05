package org.jboss.certificate.tracker.extension;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

public class PKIClientTimeIntervalHandler extends AbstractWriteAttributeHandler<Void> {

    public static final PKIClientTimeIntervalHandler INSTANCE = new PKIClientTimeIntervalHandler();

    private PKIClientTimeIntervalHandler() {
        super(PKIClientDefinition.TIME_INTERVAL);
    }

    @Override
    protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName, ModelNode resolvedValue,
            ModelNode currentValue, org.jboss.as.controller.AbstractWriteAttributeHandler.HandbackHolder<Void> handbackHolder)
            throws OperationFailedException {

        modifyTimeInterval(context, operation, resolvedValue.asLong());
        return false;
    }

    @Override
    protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName, ModelNode valueToRestore,
            ModelNode valueToRevert, Void handback) throws OperationFailedException {
        
        modifyTimeInterval(context, operation, valueToRestore.asLong());

    }

    private void modifyTimeInterval(OperationContext context, ModelNode operation, long value) {

        CertificateTrackingService service = (CertificateTrackingService) context.getServiceRegistry(true)
                .getRequiredService(CertificateTrackingService.getServiceName()).getValue();
        service.setTimeInterval(value);
    }

}
