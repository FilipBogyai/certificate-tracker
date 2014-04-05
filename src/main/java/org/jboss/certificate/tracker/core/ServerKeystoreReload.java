package org.jboss.certificate.tracker.core;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ALLOW_RESOURCE_SERVICE_RESTART;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.AUTHENTICATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.AUTHORIZATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SECURITY_REALM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_IDENTITY;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SSL;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TRUSTSTORE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

import java.io.IOException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

public class ServerKeystoreReload {

    private final Logger log = Logger.getLogger(ServerKeystoreReload.class);
    private static ModelControllerClient client;

    public static final ServerKeystoreReload INSTANCE = new ServerKeystoreReload();

    public void setManagementClient(ModelControllerClient controllerClient) {
        client = controllerClient;
    }
    
    public void reloadKeystore(String keystorePath) throws IOException {

        checkSecurityRealms(keystorePath);
        checkSecurityDomains(keystorePath);
    }

    public void checkSecurityRealms(String keystorePath) throws IOException {

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(CORE_SERVICE, MANAGEMENT));
        ModelNode readOpp = new ModelNode();
        readOpp.get(OP).set(READ_CHILDREN_NAMES_OPERATION);
        readOpp.get(CHILD_TYPE).set(SECURITY_REALM);
        readOpp.get(OP_ADDR).set(address.toModelNode());
        ModelNode result = client.execute(readOpp);

        ModelNode securityRealms = result.get("result");
        int i = 0;
        while (securityRealms.has(i)) {

            String securityRealm = securityRealms.get(i).asString();
            i++;

            ModelNode readSSLOpp = new ModelNode();
            readSSLOpp.get(OP).set(READ_ATTRIBUTE_OPERATION);
            readSSLOpp.get(NAME).set("keystore-path");

            // read serverIdentity keystore-path
            PathAddress serverIdentityAdress = PathAddress.pathAddress(PathElement.pathElement(CORE_SERVICE, MANAGEMENT),
                    PathElement.pathElement(SECURITY_REALM, securityRealm),
                    PathElement.pathElement(SERVER_IDENTITY, SSL));

            readSSLOpp.get(OP_ADDR).set(serverIdentityAdress.toModelNode());
            result = client.execute(readSSLOpp);
            String serverIdentityKeystore = result.get("result").asString();

            // if there is same keystore, then reload service and continue
            if (keystorePath.equals(serverIdentityKeystore)) {
                log.info("Reloading Server Identity for Security Realm: " + securityRealm);
                reloadKeystoreInAddress(serverIdentityAdress, keystorePath);
                continue;
            }

            // read authentication Truststore keystore-path
            PathAddress authenticationTruststoreAddress = PathAddress.pathAddress(PathElement.pathElement(CORE_SERVICE, MANAGEMENT),
                    PathElement.pathElement(SECURITY_REALM, securityRealm), PathElement.pathElement(AUTHENTICATION, TRUSTSTORE));

            readSSLOpp.get(OP_ADDR).set(authenticationTruststoreAddress.toModelNode());
            result = client.execute(readSSLOpp);
            String authenticationTruststore = result.get("result").asString();

            if (keystorePath.equals(authenticationTruststore)) {
                log.info("Reloading authentication Truststore for Security Realm: " + securityRealm);
                reloadKeystoreInAddress(authenticationTruststoreAddress, keystorePath);
                continue;
            }

            // read authorization Truststore keystore-path
            PathAddress authorizationTruststoreAddress = PathAddress.pathAddress(PathElement.pathElement(CORE_SERVICE, MANAGEMENT),
                    PathElement.pathElement(SECURITY_REALM, securityRealm), PathElement.pathElement(AUTHORIZATION, TRUSTSTORE));

            readSSLOpp.get(OP_ADDR).set(authorizationTruststoreAddress.toModelNode());
            result = client.execute(readSSLOpp);
            String authorizationTruststore = result.get("result").asString();

            if (keystorePath.equals(authorizationTruststore)) {
                log.info("Reloading authorization Truststore for Security Realm: " + securityRealm);
                reloadKeystoreInAddress(authorizationTruststoreAddress, keystorePath);
            }
        }
    }

    public void reloadKeystoreInAddress(PathAddress address, String keystorePath) throws IOException {

        ModelNode reloadOpp = new ModelNode();
        reloadOpp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        reloadOpp.get(NAME).set("keystore-path");
        reloadOpp.get(VALUE).set(keystorePath);
        reloadOpp.get(OPERATION_HEADERS, ALLOW_RESOURCE_SERVICE_RESTART).set(true);
        reloadOpp.get(OP_ADDR).set(address.toModelNode());
        ModelNode result = client.execute(reloadOpp);
        log.debug(result.toString());
    }

    public void checkSecurityDomains(String keystorePath) throws IOException {
        
        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "security"));
        ModelNode readOpp = new ModelNode();
        readOpp.get(OP).set(READ_CHILDREN_NAMES_OPERATION);
        readOpp.get(CHILD_TYPE).set("security-domain");
        readOpp.get(OP_ADDR).set(address.toModelNode());
        ModelNode result = client.execute(readOpp);

        ModelNode securityDomains = result.get("result");
        int i = 0;
        while (securityDomains.has(i)) {

            String securityDomain = securityDomains.get(i).asString();
            i++;

            // read jsse trustore-path
            PathAddress jsseTruststoreAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "security"),
                    PathElement.pathElement("security-domain", securityDomain), PathElement.pathElement("jsse", "classic"));

            ModelNode readJSSEOpp = new ModelNode();
            readJSSEOpp.get(OP).set(READ_ATTRIBUTE_OPERATION);
            readJSSEOpp.get(NAME).set("truststore");
            readJSSEOpp.get(OP_ADDR).set(jsseTruststoreAddress.toModelNode());
            result = client.execute(readJSSEOpp);
            String jsseConfig = result.get("result").asString();

            if (jsseConfig.contains(keystorePath)) {

                ModelNode reloadOpp = new ModelNode();
                reloadOpp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
                reloadOpp.get(NAME).set("truststore");
                reloadOpp.get(VALUE).set(jsseConfig);
                reloadOpp.get(OPERATION_HEADERS, ALLOW_RESOURCE_SERVICE_RESTART).set(true);
                reloadOpp.get(OP_ADDR).set(jsseTruststoreAddress.toModelNode());
                ModelNode reloadResult = client.execute(reloadOpp);
                log.debug(reloadResult.toString());

            }
        }
    }
}
