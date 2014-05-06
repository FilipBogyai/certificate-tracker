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

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

public class PKIClientRemoveHandler extends AbstractRemoveStepHandler {

    public static final PKIClientRemoveHandler INSTANCE = new PKIClientRemoveHandler();

    private PKIClientRemoveHandler() {

    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {

        String pkiClientName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        CertificateTrackerLogger.LOGGER.removingPKIClient(pkiClientName);

        ServiceName name = CertificateTrackingService.getServiceName();
        context.removeService(name);

        ServiceName managementServiceName = ManagementService.getServiceName();
        context.removeService(managementServiceName);
    }
}

