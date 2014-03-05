package org.jboss.certificate.tracker.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;



/**
 * @author Filip Bogyai
 */
public class SubsystemExtension implements Extension {

    /**
     * The name space used for the {@code substystem} element
     */
    public static final String NAMESPACE = "urn:org.jboss.certificate-tracker:1.0";

    /**
     * The name of our subsystem within the model.
     */
    public static final String SUBSYSTEM_NAME = "certificate-tracker";

    /**
     * The parser used for parsing our subsystem
     */
    private final SubsystemParser parser = new SubsystemParser();

    protected static final String KEYSTORES = "keystores";
    protected static final String PATH = "path";
    protected static final String KEYSTORE = "keystore";
    protected static final String PASSWORD = "password";
    protected static final String TYPE = "type";
    protected static final String ALIASES = "aliases";
    protected static final String PKI_CLIENT = "pki-client";
    protected static final String TIME_INTERVAL = "time-interval";
    protected static final String URL = "url";

    protected static final PathElement PKI_CLIENT_PATH = PathElement.pathElement(PKI_CLIENT);
    protected static final PathElement KEYSTORE_PATH = PathElement.pathElement(KEYSTORE);
    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    private static final String RESOURCE_NAME = SubsystemExtension.class.getPackage().getName() + ".LocalDescriptions";

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, SubsystemExtension.class.getClassLoader(), true, false);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }


    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SubsystemDefinition.INSTANCE);

        registration.registerSubModel(KeystoreDefinition.INSTANCE);
        registration.registerSubModel(PKIClientDefinition.INSTANCE);

        subsystem.registerXMLElementWriter(parser);
    }

    /**
     * The subsystem parser, which uses stax to read and write to and from xml
     */
    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
            context.startSubsystemElement(SubsystemExtension.NAMESPACE, false);
            writer.writeStartElement(KEYSTORES);
            ModelNode node = context.getModelNode();
            ModelNode path = node.get(KEYSTORE);
            for (Property property : path.asPropertyList()) {

                writer.writeStartElement(KEYSTORE);
                writer.writeAttribute(PATH, property.getName());
                ModelNode entry = property.getValue();
                KeystoreDefinition.TYPE.marshallAsAttribute(entry, true, writer);

                ModelNode entry2 = property.getValue();
                KeystoreDefinition.PASSWORD.marshallAsAttribute(entry2, true, writer);

                ModelNode entry3 = property.getValue();
                KeystoreDefinition.ALIASES.marshallAsAttribute(entry3, true, writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();

            ModelNode url = node.get(PKI_CLIENT);
            Property property = url.asProperty();

            writer.writeStartElement(PKI_CLIENT);
            writer.writeAttribute(URL, property.getName());

            ModelNode entry = property.getValue();
            PKIClientDefinition.TIME_INTERVAL.marshallAsAttribute(entry, true, writer);
            writer.writeEndElement();

            writer.writeEndElement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            // Require no content
            ParseUtils.requireNoAttributes(reader);

            final ModelNode subsystem = new ModelNode();
            subsystem.get(OP).set(ADD);
            subsystem.get(OP_ADDR).set(PathAddress.pathAddress(SUBSYSTEM_PATH).toModelNode());
            list.add(subsystem);

            // read the children
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (reader.getLocalName().equals(KEYSTORES)) {
                    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                        if (reader.isStartElement()) {
                            readKeystoreType(reader, list);
                        }
                    }
                } else if (reader.getLocalName().equals(PKI_CLIENT)) {
                    readPKIClient(reader, list);
                } else {
                    throw ParseUtils.unexpectedElement(reader);
                }

            }

        }

        private void readKeystoreType(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {

            if (!reader.getLocalName().equals(KEYSTORE)) {
                throw ParseUtils.unexpectedElement(reader);
            }

            ModelNode addKeystoreOperationModelNode = new ModelNode();
            addKeystoreOperationModelNode.get(OP).set(ADD);

            String path = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attribute = reader.getAttributeLocalName(i);
                String value = reader.getAttributeValue(i);
                if (attribute.equals(PATH)) {
                    path = value;
                } else if (attribute.equals(TYPE)) {
                    KeystoreDefinition.TYPE.parseAndSetParameter(value, addKeystoreOperationModelNode, reader);
                } else if (attribute.equals(PASSWORD)) {
                    KeystoreDefinition.PASSWORD.parseAndSetParameter(value, addKeystoreOperationModelNode, reader);
                } else if (attribute.equals(ALIASES)) {
                    KeystoreDefinition.ALIASES.parseAndSetParameter(value, addKeystoreOperationModelNode, reader);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            ParseUtils.requireNoContent(reader);
            if (path == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton(PATH));
            }

            PathAddress address = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement(KEYSTORE, path));
            addKeystoreOperationModelNode.get(OP_ADDR).set(address.toModelNode());
            list.add(addKeystoreOperationModelNode);
        }

        private void readPKIClient(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {

            ModelNode addPKIClientOperation = new ModelNode();
            addPKIClientOperation.get(OP).set(ADD);

            String url = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attribute = reader.getAttributeLocalName(i);
                String value = reader.getAttributeValue(i);
                if (attribute.equals(URL)) {
                    url = value;
                } else if (attribute.equals(TIME_INTERVAL)) {
                    PKIClientDefinition.TIME_INTERVAL.parseAndSetParameter(value, addPKIClientOperation, reader);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            ParseUtils.requireNoContent(reader);
            if (url == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton(URL));
            }

            PathAddress address = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement(PKI_CLIENT, url));
            addPKIClientOperation.get(OP_ADDR).set(address.toModelNode());
            list.add(addPKIClientOperation);
        }
    }

}
