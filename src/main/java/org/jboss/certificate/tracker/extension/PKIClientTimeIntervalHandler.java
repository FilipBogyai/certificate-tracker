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

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 * Handler responsible for changing time period in which certificates are
 * updated.
 * 
 * @author Filip Bogyai
 */
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
