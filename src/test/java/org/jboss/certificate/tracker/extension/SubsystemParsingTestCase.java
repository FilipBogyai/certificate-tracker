package org.jboss.certificate.tracker.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

import java.util.List;

import junit.framework.Assert;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

/**
 * Tests all management expects for subsystem, parsing, marshaling, model
 * definition and other Here is an example that allows you a fine grained
 * controller over what is tested and how. So it can give you ideas what can be
 * done and tested. If you have no need for advanced testing of subsystem you
 * look at {@link SubsystemBaseParsingTestCase} that tests same stuff but most
 * of the code is hidden inside of test harness
 * 
 * @author Filip Bogyai
 */
public class SubsystemParsingTestCase extends AbstractSubsystemTest {

    public SubsystemParsingTestCase() {
        super(CertificateTrackerExtension.SUBSYSTEM_NAME, new CertificateTrackerExtension());
    }
    
    public String getSubsystemXML(){
        
        return "<subsystem xmlns=\"" + CertificateTrackerExtension.NAMESPACE + "\">" +
                  "<keystores>" +
                      "<keystore name=\"example\" path=\"" + getResourcesPath("example.keystore") + "\" type=\"JKS\" password=\"secret\" />" +
                      "<keystore name=\"test\" path=\"" + getResourcesPath("test.keystore") + "\" type=\"JKS\" password=\"secret\" />" +
                  "</keystores>" +
                  "<pki-client name =\"Dogtag\"  time-interval=\"3000\" >" +
                    "<client-option name=\"url\" value=\"http://example.com\" />" +
                    "<client-option name=\"truststore-name\" value=\"example\" />" + 
                  "</pki-client>" +
                "</subsystem>";
    }

    /**
     * Tests that the xml is parsed into the correct operations
     */
    @Test
    public void testParseSubsystem() throws Exception {
        // Parse the subsystem xml into operations
        String subsystemXml = getSubsystemXML();
        List<ModelNode> operations = super.parse(subsystemXml);

        // /Check that we have the expected number of operations
        Assert.assertEquals(4, operations.size());

        // Check that each operation has the correct content
        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(CertificateTrackerExtension.SUBSYSTEM_NAME, element.getValue());

        // Then we will get the add keystore operation
        ModelNode addKeystore = operations.get(1);
        Assert.assertEquals(ADD, addKeystore.get(OP).asString());
        Assert.assertEquals(getResourcesPath("example.keystore"), addKeystore.get("path").asString());
        Assert.assertEquals("secret", addKeystore.get("password").asString());
        addr = PathAddress.pathAddress(addKeystore.get(OP_ADDR));
        Assert.assertEquals(2, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(CertificateTrackerExtension.SUBSYSTEM_NAME, element.getValue());
        element = addr.getElement(1);
        Assert.assertEquals("keystore", element.getKey());
        Assert.assertEquals("example", element.getValue());

        // Then we will get the add operation for pki-client
        ModelNode addPKIClient = operations.get(3);
        Assert.assertEquals(ADD, addPKIClient.get(OP).asString());
        Assert.assertEquals("3000", addPKIClient.get("time-interval").asString());
        addr = PathAddress.pathAddress(addPKIClient.get(OP_ADDR));
        Assert.assertEquals(2, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(CertificateTrackerExtension.SUBSYSTEM_NAME, element.getValue());
        element = addr.getElement(1);
        Assert.assertEquals("pki-client", element.getKey());
        Assert.assertEquals("Dogtag", element.getValue());

    }

    /**
     * Test that the model created from the xml looks as expected
     */
    @Test
    public void testInstallIntoController() throws Exception {
        // Parse the subsystem xml and install into the controller
        String subsystemXml = getSubsystemXML();
        KernelServices services = super.installInController(subsystemXml);

        // Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();

        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(CertificateTrackerExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME).hasDefined("keystore"));
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore").hasDefined("example"));
        
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore", "example").hasDefined("path"));
        Assert.assertEquals(getResourcesPath("example.keystore"), model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore", "example", "path").asString());
        
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore", "example").hasDefined("password"));
        Assert.assertEquals("secret", model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore", "example", "password").asString());
        
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore", "example").hasDefined("type"));
        Assert.assertEquals("JKS", model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore", "example", "type").asString());
        
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME).hasDefined("pki-client"));
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client").hasDefined("Dogtag"));

        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client", "Dogtag").hasDefined("time-interval"));
        Assert.assertEquals("3000",
                model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client", "Dogtag", "time-interval").asString());
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client", "Dogtag").hasDefined(
                "client-options"));
        Assert.assertEquals("(\"url\" => \"http://example.com\")",
                model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client", "Dogtag", "client-options").asList().get(0).toString());
        Assert.assertEquals("(\"truststore-name\" => \"example\")",
                model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client", "Dogtag", "client-options").asList().get(1).toString());
    }

    /**
     * Starts a controller with a given subsystem xml and then checks that a
     * second controller started with the xml marshalled from the first one
     * results in the same model
     */
    @Test
    public void testParseAndMarshalModel() throws Exception {
        // Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXML();
        KernelServices servicesA = super.installInController(subsystemXml);
        // Get the model and the persisted xml from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        String marshalled = servicesA.getPersistedSubsystemXml();
        // Install the persisted xml from the first controller into a second
        // controller
        KernelServices servicesB = super.installInController(marshalled);
        ModelNode modelB = servicesB.readWholeModel();

        // Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Starts a controller with the given subsystem xml and then checks that a
     * second controller started with the operations from its describe action
     * results in the same model
     */
    @Test
    public void testDescribeHandler() throws Exception {
        // Parse the subsystem xml and install into the first controller
        String subsystemXml = "<subsystem xmlns=\"" + CertificateTrackerExtension.NAMESPACE + "\">" + "</subsystem>";
        KernelServices servicesA = super.installInController(subsystemXml);
        // Get the model and the describe operations from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME)).toModelNode());
        List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();

        // Install the describe options from the first controller into a second
        // controller
        KernelServices servicesB = super.installInController(operations);
        ModelNode modelB = servicesB.readWholeModel();

        // Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Tests that the subsystem can be removed
     */
    @Test
    public void testSubsystemRemoval() throws Exception {
        // Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXML();
        KernelServices services = super.installInController(subsystemXml);

        services.getContainer().getRequiredService(CertificateTrackingService.getServiceName());

        // Checks that the subsystem was removed from the model
        super.assertRemoveSubsystemResources(services);

        // Check that any services that were installed were removed here
        try {
            services.getContainer().getRequiredService(CertificateTrackingService.getServiceName());
            Assert.fail("Should have removed services");
        } catch (Exception expected) {
            // OK
        }
    }

    @Test
    public void testExecuteOperations() throws Exception {

        String subsystemXml = "<subsystem xmlns=\"" + CertificateTrackerExtension.NAMESPACE + "\">" + "<keystores>" + "<keystore name=\"example\" path=\""
                + getResourcesPath("example.keystore") + "\" type=\"JKS\" password=\"secret\" />" + "</keystores>"
                + "<pki-client name=\"Dogtag\" >" +
                    "<client-option name=\"url\" value=\"http://example.com\" />" +
                    "<client-option name=\"truststore-name\" value=\"example\" />" + 
                  "</pki-client>" + "</subsystem>";

        KernelServices services = super.installInController(subsystemXml);

        PathAddress keystore2Address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("keystore", "test"));
        ModelNode addOp = new ModelNode();
        addOp.get(OP).set(ADD);
        addOp.get(OP_ADDR).set(keystore2Address.toModelNode());
        addOp.get("path").set(getResourcesPath("test.keystore"));
        addOp.get("password").set("secret");

        ModelNode result = services.executeOperation(addOp);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());

        ModelNode model = services.readWholeModel();

        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(CertificateTrackerExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME).hasDefined("keystore"));
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore").hasDefined("test"));
        // check default type="JKS"
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore", "test").hasDefined("type"));
        Assert.assertEquals("JKS", model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "keystore", "test", "type").asString());

        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME).hasDefined("pki-client"));
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client").hasDefined("Dogtag"));
        // check default time-interval=60000
        Assert.assertTrue(model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client", "Dogtag").hasDefined(
                "time-interval"));
        Assert.assertEquals(PKIClientDefinition.DEFAULT_TIME_INTERVAL,
                model.get(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME, "pki-client", "Dogtag", "time-interval").asLong());

        PathAddress pkiClientAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("pki-client", "Dogtag"));
        // Call write-attribute
        ModelNode writeOp = new ModelNode();
        writeOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        writeOp.get(OP_ADDR).set(pkiClientAddress.toModelNode());
        writeOp.get(NAME).set("time-interval");
        writeOp.get(VALUE).set(8000);
        result = services.executeOperation(writeOp);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());

        // Check that write attribute took effect, this time by calling
        // read-attribute instead of reading the whole model
        ModelNode readOp = new ModelNode();
        readOp.get(OP).set(READ_ATTRIBUTE_OPERATION);
        readOp.get(OP_ADDR).set(pkiClientAddress.toModelNode());
        readOp.get(NAME).set("time-interval");
        result = services.executeOperation(readOp);
        Assert.assertEquals(8000, checkResultAndGetContents(result).asLong());

        CertificateTrackingService service = (CertificateTrackingService) services.getContainer()
                .getRequiredService(CertificateTrackingService.getServiceName()).getValue();
        Assert.assertEquals(8000, service.getTimeInterval());

    }

    @Test
    public void testChangeKeystorePassword() throws Exception {
        String subsystemXml = getSubsystemXML();

        KernelServices services = super.installInController(subsystemXml);

        PathAddress exampleAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, CertificateTrackerExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("keystore", "example"));

        ModelNode writeOp = new ModelNode();
        writeOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        writeOp.get(OP_ADDR).set(exampleAddress.toModelNode());
        writeOp.get(NAME).set("password");
        writeOp.get(VALUE).set("123456");
        ModelNode result = services.executeOperation(writeOp);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());

    }

    private String getResourcesPath(String fileName) {

        return SubsystemParsingTestCase.class.getResource(fileName).getPath();
    }

}