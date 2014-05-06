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

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.certificate.tracker.core.KeystoresTrackingManager;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

public class KeystoreAddHandler extends AbstractAddStepHandler {

    public static final KeystoreAddHandler INSTANCE = new KeystoreAddHandler();

    private KeystoreAddHandler() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        KeystoreDefinition.PATH.validateAndSet(operation, model);
        KeystoreDefinition.PASSWORD.validateAndSet(operation, model);
        KeystoreDefinition.TYPE.validateAndSet(operation, model);
        KeystoreDefinition.ALIASES.validateAndSet(operation, model);

    }

    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model,
            final ServiceVerificationHandler verificationHandler, final List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        String name = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        String path = KeystoreDefinition.PATH.resolveModelAttribute(context, model).asString();
        String password = KeystoreDefinition.PASSWORD.resolveModelAttribute(context, model).asString();
        String type = KeystoreDefinition.TYPE.resolveModelAttribute(context, model).asString();

        ModelNode aliasesNode = KeystoreDefinition.ALIASES.resolveModelAttribute(context, model);
        String aliases = aliasesNode.isDefined() ? aliasesNode.toString() : null;

        // add keystore manager to service
        CertificateTrackerLogger.LOGGER.addingNewKeystore(name, path);
        KeystoresTrackingManager.INSTANCE.addKeystore(name, path, type, password, aliases);

    }
}
