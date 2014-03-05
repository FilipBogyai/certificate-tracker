package org.jboss.certificate.tracker.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;

/**
 * Handler responsible for removing the certificate tracker subsystem resource
 * from the model
 * 
 * @author Filip Bogyai
 */
class SubsystemRemove extends AbstractRemoveStepHandler {

    static final SubsystemRemove INSTANCE = new SubsystemRemove();

    private SubsystemRemove() {
    }

}
