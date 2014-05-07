package org.jboss.certificate.tracker.client.dogtag;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;

public class ObjectMarshallingTestCase {

    /**
     * Tests that REST transfer objects are correctly marshalled from the xml
     */
    @Test
    public void testCertDataMarshalling() throws Exception {
        
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

        CertData before = new CertData();
        before.setSerialNumber(new CertId("12512514865863765114"));
        before.setIssuerDN("CN=Test User,UID=testuser,O=EXAMPLE-COM");
        before.setSubjectDN("CN=Test User,UID=testuser,O=EXAMPLE-COM");
        before.setEncoded(sw.toString());
        before.setNonce(12345l);

        String string = before.toString();
        System.out.println(string);

        CertData after = CertData.valueOf(string);
        Assert.assertEquals(before, after);
        
    }
    
    /**
     * Tests that {@link CertDataInfo} REST transfer object is correctly
     * marshalled from the xml
     */
    @Test
    public void testCertDataInfoMarshalling() throws Exception {
        
        CertDataInfo before = new CertDataInfo();
        before.setID(new CertId("12512514865863765114"));
        before.setSubjectDN("CN=Test User,UID=testuser,O=EXAMPLE-COM");
        before.setStatus("VALID");

        String string = before.toString();
        System.out.println(string);

        CertDataInfo after = CertDataInfo.valueOf(string);

        Assert.assertEquals(before, after);
        
    }
    
}
