package org.jboss.certificate.tracker.client.dogtag;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.certificate.tracker.client.DogtagPKIClient;
import org.jboss.certificate.tracker.core.PKIClient;
import org.junit.Test;

public class DogtagPKIClientTestCase {

    /**
     * Tests initalization of Dogtag PKI client
     */
    @Test
    public void testInitialization() throws Exception {

        PKIClient pkiClient = new DogtagPKIClient();

        Map<String, Object> options = new HashMap<String, Object>();

        pkiClient.init(options);
        // without missing url option client cannot be initalized
        Assert.assertFalse(pkiClient.isInitialized());

        options.put("url", "example.com");

        pkiClient.init(options);
        Assert.assertTrue(pkiClient.isInitialized());

    }

    /**
     * Tests that certificate is correctly loaded from binary representation
     */
    @Test
    public void testLoadBinaryCertificate() throws Exception {

        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw, true);

        out.println("-----BEGIN CERTIFICATE-----");
        out.println("MIIB/zCCAWgCCQCtpWH58pqsejANBgkqhkiG9w0BAQUFADBEMRQwEgYDVQQKDAtF");
        out.println("WEFNUExFLUNPTTEYMBYGCgmSJomT8ixkAQEMCHRlc3R1c2VyMRIwEAYDVQQDDAlU");
        out.println("ZXN0IFVzZXIwHhcNMTIwNTE0MTcxNzI3WhcNMTMwNTE0MTcxNzI3WjBEMRQwEgYD");
        out.println("VQQKDAtFWEFNUExFLUNPTTEYMBYGCgmSJomT8ixkAQEMCHRlc3R1c2VyMRIwEAYD");
        out.println("VQQDDAlUZXN0IFVzZXIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKmmiPJp");
        out.println("Agh/gPUAZjfgJ3a8QiHvpMzZ/hZy1FVP3+2sNhCkMv+D/I8Y7AsrbJGxxvD7bTDm");
        out.println("zQYtYx2ryGyOgY7KBRxEj/IrNVHIkJMYq5G/aIU4FAzpc6ntNSwUQBYUAamfK8U6");
        out.println("Wo4Cp6rLePXIDE6sfGn3VX6IeSJ8U2V+vwtzAgMBAAEwDQYJKoZIhvcNAQEFBQAD");
        out.println("gYEAY9bjcD/7Z+oX6gsJtX6Rd79E7X5IBdOdArYzHNE4vjdaQrZw6oCxrY8ffpKC");
        out.println("0T0q5PX9I7er+hx/sQjGPMrJDEN+vFBSNrZE7sTeLRgkyiqGvChSyuG05GtGzXO4");
        out.println("bFBr+Gwk2VF2wJvOhTXU2hN8sfkkd9clzIXuL8WCDhWk1bY=");
        out.println("-----END CERTIFICATE-----");

        byte[] source = sw.toString().getBytes();

        DogtagPKIClient client = new DogtagPKIClient();
        X509Certificate certificate = client.loadBinaryCertificate(source);

        Assert.assertEquals("12512514865863765114", certificate.getSerialNumber().toString());
        Assert.assertEquals("CN=Test User, UID=testuser, O=EXAMPLE-COM", certificate.getIssuerDN().toString());
        Assert.assertEquals("CN=Test User, UID=testuser, O=EXAMPLE-COM", certificate.getSubjectDN().toString());
    }

}
