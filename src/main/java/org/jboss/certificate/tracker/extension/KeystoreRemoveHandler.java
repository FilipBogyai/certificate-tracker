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
import org.jboss.certificate.tracker.core.KeystoresTrackingManager;
import org.jboss.dmr.ModelNode;

/**
 * Handler responsible for removing the keystore element from the model
 * 
 * @author Filip Bogyai
 */
public class KeystoreRemoveHandler extends AbstractRemoveStepHandler{

    public static final KeystoreRemoveHandler INSTANCE = new KeystoreRemoveHandler();

    private KeystoreRemoveHandler() {

    }

    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model)
            throws OperationFailedException {

        String name = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();

        CertificateTrackerLogger.LOGGER.removingKeystore(name);
        KeystoresTrackingManager.INSTANCE.removeKeystoreManager(name);
    }

}

