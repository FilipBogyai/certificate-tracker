package org.jboss.certificate.tracker.extension;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

@MessageLogger(projectCode = "CERTRACK")
public interface CertificateTrackerLogger extends BasicLogger {

    CertificateTrackerLogger LOGGER = Logger.getMessageLogger(CertificateTrackerLogger.class, CertificateTrackerLogger.class.getPackage().getName());

    @LogMessage(level = Level.INFO)
    @Message(id = 10000, value = "Checking for certificate updates")
    void checkingCertificates();

    @LogMessage(level = Level.ERROR)
    @Message(id = 10001, value = "Error while checking for certificate updates")
    void checkingCertificatesError(@Cause Throwable cause);

}
