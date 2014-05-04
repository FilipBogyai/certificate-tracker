package org.jboss.certificate.tracker.extension;

import org.jboss.as.controller.SimpleResourceDefinition;

/**
 * @author Filip Bogyai
 */
public class CertificateTrackerDefinition extends SimpleResourceDefinition {
    public static final CertificateTrackerDefinition INSTANCE = new CertificateTrackerDefinition();

    private CertificateTrackerDefinition() {
        super(CertificateTrackerExtension.SUBSYSTEM_PATH,
                CertificateTrackerExtension.getResourceDescriptionResolver(null),
                //We always need to add an 'add' operation
                CertificateTrackerAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                CertificateTrackerRemove.INSTANCE);
    }

}
