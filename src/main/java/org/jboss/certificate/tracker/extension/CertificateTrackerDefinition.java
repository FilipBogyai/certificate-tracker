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

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
 * Root definition of certificate-tracker subsystem
 * 
 * @author Filip Bogyai
 */
public class CertificateTrackerDefinition extends SimpleResourceDefinition {
    public static final CertificateTrackerDefinition INSTANCE = new CertificateTrackerDefinition();

    private CertificateTrackerDefinition() {
        super(CertificateTrackerExtension.SUBSYSTEM_PATH,
                CertificateTrackerExtension.getResourceDescriptionResolver(null),
                CertificateTrackerAdd.INSTANCE, CertificateTrackerRemove.INSTANCE);
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
