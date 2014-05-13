# Certificate-Tracker is an Wildfly extension for certificate management

This extension is able to obtain and store updated SSL certificates from certificate system into keystores, which are available to WildFly Application Server. After update it restart dependant services of WildFly to use updated certificates.

For tracking certificate it uses PKI client, which is connected to certificate system. Current version supports PKI client for Dogtag Certficate System, which is part of identity management server FreeIPA. It uses REST interface to track and obtain these certificates.

## How to get it

You need to have [git](http://git-scm.com/) installed

	$ git clone https://github.com/fbogyai/certificate-tracker.git

## How to build it

You need to have [Maven](http://maven.apache.org/) installed

	$ cd certificate-tracker
	$ mvn clean install

## How to install it

Copy the produced module to the WildFly modules (set correct path to `$JBOSS_HOME`):

	$ JBOSS_HOME=/home/wildfly
	$ cp -R target/modules/* "$JBOSS_HOME/modules"

## How add extension to WildFly configuration

Use the CLI -  `jboss-cli.sh` (or `.bat`). Add the certificate-tracker extension and subsystem to WildFly configuration.

	/extension=org.jboss.certificate-tracker:add()
	/subsystem=certificate-tracker:add()
	
## How to use it

Use the CLI -  `jboss-cli.sh` (or `.bat`) to configure extension.

### Configration of tracked keystores

To register a keystore into certificate-tracker use:

	/subsystem=certificate-tracker/keystore=example:add(path=$JBOSS_HOME/example.jks, type=JKS, password=secret, aliases="host1,host2")

This will add keystore with defined path, type, password and optionally you can choose only some aliases which will be managed.

### Configration of PKI client

To configure a PKI client to track certificates use:

	/subsystem=certificate-tracker/pki-client=Dogtag:add(time-interval="5000", client-options={"url"=>https://example.com/ca/rest","truststore-name"=>"example"}

This will add Dogtag PKI client, which will track certificates in 5000ms interval. PKI client is initialized with client-options. Dogtag PKI client accepts options "url" and "truststore-name" with trusted certificate of Dogtag for use of secure connection. 

## Exaple XML configuration

	<subsystem xmlns="urn:org.jboss.certificate-tracker:1.0">
	  <keystores>
	    <keystore name="example" path="$JBOSS_HOME/example.jks" type="JKS" password="secret" aliases="host1,host2"/>
	  </keystores>
	  <pki-client name="Dogtag"/>
	    <client-option name="url" value="https://example.com/ca/rest"/>
	    <client-option name="truststore-name" value="example"/>
	  </pki-client>
	</subsystem>

## License

* [GNU Lesser General Public License Version 3](http://www.gnu.org/licenses/lgpl.html)








