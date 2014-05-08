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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.Attribute;
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
 * Main entry point for certificate-trakcer extension, which registers subsystem
 * model. Contains parser for reading and writing configuration.
 * 
 * @author Filip Bogyai
 */
public class CertificateTrackerExtension implements Extension {

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
    protected static final String NAME = "name";
    protected static final String PATH = "path";
    protected static final String KEYSTORE = "keystore";
    protected static final String PASSWORD = "password";
    protected static final String TYPE = "type";
    protected static final String ALIASES = "aliases";
    protected static final String PKI_CLIENT = "pki-client";
    protected static final String TIME_INTERVAL = "time-interval";
    protected static final String MODULE = "module";
    protected static final String CLIENT_OPTIONS = "client-options";
    protected static final String CLIENT_OPTION = "client-option";

    protected static final PathElement PKI_CLIENT_PATH = PathElement.pathElement(PKI_CLIENT);
    protected static final PathElement KEYSTORE_PATH = PathElement.pathElement(KEYSTORE);
    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    private static final String RESOURCE_NAME = CertificateTrackerExtension.class.getPackage().getName() + ".LocalDescriptions";

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, CertificateTrackerExtension.class.getClassLoader(), true, false);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }


    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(CertificateTrackerDefinition.INSTANCE);

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
            context.startSubsystemElement(CertificateTrackerExtension.NAMESPACE, false);
            writer.writeStartElement(KEYSTORES);
            ModelNode node = context.getModelNode();
            ModelNode path = node.get(KEYSTORE);
            for (Property property : path.asPropertyList()) {

                writer.writeStartElement(KEYSTORE);
                writer.writeAttribute(NAME, property.getName());

                ModelNode entry = property.getValue();
                KeystoreDefinition.PATH.marshallAsAttribute(entry, true, writer);
                KeystoreDefinition.TYPE.marshallAsAttribute(entry, true, writer);
                KeystoreDefinition.PASSWORD.marshallAsAttribute(entry, true, writer);
                KeystoreDefinition.ALIASES.marshallAsAttribute(entry, true, writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();

            ModelNode name = node.get(PKI_CLIENT);
            Property property = name.asProperty();

            writer.writeStartElement(PKI_CLIENT);
            writer.writeAttribute(NAME, property.getName());

            ModelNode entry = property.getValue();
            PKIClientDefinition.TIME_INTERVAL.marshallAsAttribute(entry, true, writer);
            PKIClientDefinition.MODULE.marshallAsAttribute(entry, true, writer);

            if (entry.hasDefined(CLIENT_OPTIONS)) {
                ModelNode properties = entry.get(CLIENT_OPTIONS);
                for (Property prop : properties.asPropertyList()){
                    writer.writeEmptyElement(CLIENT_OPTION);
                    writer.writeAttribute(Attribute.NAME.getLocalName(), prop.getName());
                    writer.writeAttribute(Attribute.VALUE.getLocalName(), prop.getValue().asString());
                    
                }
            }
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

            String name = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attribute = reader.getAttributeLocalName(i);
                String value = reader.getAttributeValue(i);

                if (attribute.equals(NAME)) {
                    name = value;
                } else if (attribute.equals(PATH)) {
                    KeystoreDefinition.PATH.parseAndSetParameter(value, addKeystoreOperationModelNode, reader);
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
            if (name == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton(PATH));
            }

            PathAddress address = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement(KEYSTORE, name));
            addKeystoreOperationModelNode.get(OP_ADDR).set(address.toModelNode());
            list.add(addKeystoreOperationModelNode);
        }

        private void readPKIClient(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {

            ModelNode addPKIClientOperation = new ModelNode();
            addPKIClientOperation.get(OP).set(ADD);

            String name = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String attribute = reader.getAttributeLocalName(i);
                String value = reader.getAttributeValue(i);
                if (attribute.equals(NAME)) {
                    name = value;
                } else if (attribute.equals(TIME_INTERVAL)) {
                    PKIClientDefinition.TIME_INTERVAL.parseAndSetParameter(value, addPKIClientOperation, reader);              
                } else if (attribute.equals(MODULE)) {
                    PKIClientDefinition.MODULE.parseAndSetParameter(value, addPKIClientOperation, reader);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            
            if (name == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton(NAME));
            }
            
            while(reader.hasNext() && reader.nextTag() != END_ELEMENT){
                if (reader.getLocalName().equals(CLIENT_OPTION)) {
                    readPKIClientOption(reader, addPKIClientOperation.get(CLIENT_OPTIONS));
                } else {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
                     
            PathAddress address = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement(PKI_CLIENT, name));
            addPKIClientOperation.get(OP_ADDR).set(address.toModelNode());
            list.add(addPKIClientOperation);
        }
        
        private void readPKIClientOption(XMLExtendedStreamReader reader, ModelNode clientOptions) throws XMLStreamException {
            
            String name = null;
            String val = null;
            EnumSet<Attribute> required = EnumSet.of(Attribute.NAME, Attribute.VALUE);
            final int count = reader.getAttributeCount();
            for (int i = 0; i < count; i++) {

                final String value = reader.getAttributeValue(i);
                final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                required.remove(attribute);
                switch (attribute) {
                case NAME: {
                    name = value;
                    break;
                }
                case VALUE: {
                    val = value;
                    break;
                }
                default:
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }

            if (required.size() > 0) {
                throw ParseUtils.missingRequired(reader, required);
            }

            clientOptions.add(name, val);
            ParseUtils.requireNoContent(reader);
            
        }
    }
}
