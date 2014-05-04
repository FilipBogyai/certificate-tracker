package org.jboss.certificate.tracker.extension;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "CERTRACK")
public interface CertificateTrackerLogger extends BasicLogger {

    CertificateTrackerLogger LOGGER = Logger.getMessageLogger(CertificateTrackerLogger.class, CertificateTrackerLogger.class.getPackage().getName());

    @LogMessage(level = Level.INFO)
    @Message(id = 10000, value = "Checking for certificate updates")
    void checkingCertificates();

    @LogMessage(level = Level.ERROR)
    @Message(id = 10001, value = "Error while checking for certificate updates")
    void checkingCertificatesError(@Cause Throwable cause);

    @LogMessage(level = Level.INFO)
    @Message(id = 10002, value = "Adding new keystore to certificate-tracker: %s : %s")
    void addingNewKeystore(String name, String path);

    @LogMessage(level = Level.INFO)
    @Message(id = 10003, value = "Removing keystore from certificate-tracker: %s")
    void removingKeystore(String name);

    @LogMessage(level = Level.INFO)
    @Message(id = 10004, value = "Starting PKI client %s to check certificates with time interval %s ms")
    void addingPKIClient(String name, String time);

    @LogMessage(level = Level.INFO)
    @Message(id = 10005, value = "Stopping PKI client %s")
    void removingPKIClient(String name);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10006, value = "URL address of Certificate System is invalid")
    void invalidURL(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10007, value = "Unable to create trustStore from keystore manager: %s")
    void unableToCreateTruststore(String name, @Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10008, value = "Certificate with alias: %s was not found in keystore: %s")
    void noCertificateWithAlias(String alias, String keystore);

    @LogMessage(level = Level.INFO)
    @Message(id = 10009, value = "Certificate for KeyPair with alias: %s has been updated in keystore: %s")
    void updatedKeyCertificate(String alias, String keystore);

    @LogMessage(level = Level.INFO)
    @Message(id = 10010, value = "Certificate with alias: %s has been updated in keystore: %s")
    void updatedCertificate(String alias, String keystore);

    @LogMessage(level = Level.WARN)
    @Message(id = 10011, value = "New certificate for SubjectDN: %s is available, but has different KeyPair. To update please import new KeyPair.")
    void differentKeyCertificate(String subjectDN);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10012, value = "Unable to save keystore %s")
    void unableToSaveKeystore(String name, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 10013, value = "Cannot establish trusted path to root CA in new certificate with alias: %s")
    void untrustedRootCA(String alias);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10014, value = "Cannot load module for custom PKIClient")
    void moduleNotFound(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10015, value = "Cannot obtain certificates from keystore: %s")
    void unableToLoadCertificates(String name, @Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10016, value = "Unable to update certificate with alias: %s in keystore: %s")
    void unableToUpdateCertificate(String alias, String keystore, @Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10017, value = "Cannot load certificates")
    void cannotLoadCertificates(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10018, value = "Cannot copy certificates")
    void cannotCopyCertificates(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10019, value = "Cannot load keystore")
    void cannotLoadKeystore(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10020, value = "Cannot load CA keystore")
    void cannotLoadCAKeystore(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10021, value = "Cannot load certificate")
    void cannotLoadCertificate(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10022, value = "Cannot load certificate from binary format")
    void cannotLoadBinaryCertificate(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10023, value = "Cannot encrypt with certificate")
    void cannotEncryptWithCertificate(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10024, value = "Unable to find PKIClient class")
    void unableToFindClass(@Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 10025, value = "Unable to create new PKI Client")
    void unableToCreateClass(@Cause Throwable cause);

    @LogMessage(level = Level.INFO)
    @Message(id = 10026, value = "Reloading Server Identity for Security Realm: %s")
    void reloadingIdentity(String securityRealm);

    @LogMessage(level = Level.INFO)
    @Message(id = 10027, value = "Reloading authentication Truststore for Security Realm: %s")
    void reloadingTrustStore(String securityRealm);

    @LogMessage(level = Level.INFO)
    @Message(id = 10028, value = "Reloading authorization Truststore for Security Realm: %s")
    void reloadingAuthorzTrustStore(String securityRealm);

}

