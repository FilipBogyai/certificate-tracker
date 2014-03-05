package org.jboss.certificate.tracker.client.service;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ALLOW_RESOURCE_SERVICE_RESTART;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SECURITY_REALM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_IDENTITY;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SSL;

import java.io.IOException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

public class ReloadKeystoreService {

    private final Logger log = Logger.getLogger(ReloadKeystoreService.class);
    private static ModelControllerClient client;

    public static final ReloadKeystoreService INSTANCE = new ReloadKeystoreService();

    public void setManagementClient(ModelControllerClient controllerClient) {
        client = controllerClient;
    }
    
    public void reloadKeystore(String keystorePath) throws IOException {

        checkSecurityRealms(keystorePath);
        // maybe check also security domain??
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

            PathAddress securityRealmAddress = PathAddress.pathAddress(PathElement.pathElement(CORE_SERVICE, MANAGEMENT),
                    PathElement.pathElement(SECURITY_REALM, securityRealm),
                    PathElement.pathElement(SERVER_IDENTITY, SSL));

            ModelNode readSSLOpp = new ModelNode();
            readSSLOpp.get(OP).set(READ_ATTRIBUTE_OPERATION);
            readSSLOpp.get(NAME).set("keystore-path");
            readSSLOpp.get(OP_ADDR).set(securityRealmAddress.toModelNode());
            result = client.execute(readSSLOpp);

            String keystore = result.get("result").asString();

            if (keystorePath.equals(keystore)) {

                ModelNode readPassOpp = new ModelNode();
                readPassOpp.get(OP).set(READ_ATTRIBUTE_OPERATION);
                readPassOpp.get(NAME).set("keystore-password");
                readPassOpp.get(OP_ADDR).set(securityRealmAddress.toModelNode());
                result = client.execute(readPassOpp);

                String password = result.get("result").asString();

                reloadServerIdentity(securityRealm, keystorePath, password);
            }

            i++;
        }
    }

    public void reloadServerIdentity(String securityRealm, String keystorePath, String keystorePassword) throws IOException {
        
        log.info("Reloading Server Identity for Security Realm: " + securityRealm);

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(CORE_SERVICE, MANAGEMENT),PathElement.pathElement(SECURITY_REALM, securityRealm),
                PathElement.pathElement(SERVER_IDENTITY,SSL));
        ModelNode removeOpp = new ModelNode();
        removeOpp.get(OP).set(REMOVE);
        removeOpp.get(OPERATION_HEADERS, ALLOW_RESOURCE_SERVICE_RESTART).set(true);
        removeOpp.get(OP_ADDR).set(address.toModelNode());
        ModelNode result = client.execute(removeOpp);
        log.debug(result.toString());
        
        ModelNode addOpp = new ModelNode();
        addOpp.get(OP).set(ADD);
        addOpp.get("keystore-path").set(keystorePath);
        addOpp.get("keystore-password").set(keystorePassword);
        addOpp.get(OPERATION_HEADERS, ALLOW_RESOURCE_SERVICE_RESTART).set(true);
        addOpp.get(OP_ADDR).set(address.toModelNode());
        result = client.execute(addOpp);
        log.debug(result.toString());
        
    }
}
