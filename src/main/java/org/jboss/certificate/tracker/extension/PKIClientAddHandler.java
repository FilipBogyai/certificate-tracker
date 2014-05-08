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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

/**
 * Handler responsible for adding the pki-client element to the model
 * 
 * @author Filip Bogyai
 */
public class PKIClientAddHandler extends AbstractAddStepHandler {

    public static PKIClientAddHandler INSTANCE = new PKIClientAddHandler();

    private PKIClientAddHandler() {

    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (AttributeDefinition attr : PKIClientDefinition.ALL_ATTRIBUTES) {
            attr.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model,
            final ServiceVerificationHandler verificationHandler, final List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        String name = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        long timeInterval = PKIClientDefinition.TIME_INTERVAL.resolveModelAttribute(context, model).asLong();

        ModelNode modulNode = PKIClientDefinition.MODULE.resolveModelAttribute(context, model);
        String module = modulNode.isDefined() ? modulNode.asString() : null;

        Map<String, Object> clientOptions = new HashMap<String, Object>();
        if (operation.hasDefined(PKIClientDefinition.CLIENT_OPTIONS.getName())) {
            for (Map.Entry<String, String> clientOption : PKIClientDefinition.CLIENT_OPTIONS.unwrap(context, model).entrySet()) {
                clientOptions.put(clientOption.getKey(), clientOption.getValue());
            }
        }

        final ManagementService serverControllerService = new ManagementService();
        ServiceController<ManagementService> serverServiceController = context.getServiceTarget()
                .addService(ManagementService.getServiceName(), serverControllerService)
                .addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, serverControllerService.modelController)
                .setInitialMode(Mode.ACTIVE)
                .install();
        newControllers.add(serverServiceController);

        CertificateTrackerLogger.LOGGER.addingPKIClient(name, Long.toString(timeInterval));

        CertificateTrackingService certificateTrackingService = new CertificateTrackingService(name, timeInterval, module, clientOptions);
        ServiceName serviceName = CertificateTrackingService.getServiceName();
        ServiceController<CertificateTrackingService> serviceController = context.getServiceTarget()
                .addService(serviceName, certificateTrackingService)
                .addListener(verificationHandler)
                .setInitialMode(Mode.ACTIVE)
                .install();
        newControllers.add(serviceController);
    }

}
