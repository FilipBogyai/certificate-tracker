package org.jboss.certificate.tracker.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;

/**
 * Handler responsible for removing the certificate tracker subsystem resource
 * from the model
 * 
 * @author Filip Bogyai
 */
class CertificateTrackerRemove extends AbstractRemoveStepHandler {

    static final CertificateTrackerRemove INSTANCE = new CertificateTrackerRemove();

    private CertificateTrackerRemove() {
    }

}
