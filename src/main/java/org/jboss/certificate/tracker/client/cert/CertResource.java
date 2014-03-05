package org.jboss.certificate.tracker.client.cert;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("")
public interface CertResource {

    @GET
    @Path("certs")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CertDataInfos listCerts();

    @GET
    @Path("certs")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CertDataInfos listCerts(@QueryParam("status") String status, @QueryParam("maxResults") Integer maxResults,
            @QueryParam("maxTime") Integer maxTime, @QueryParam("start") Integer start, @QueryParam("size") Integer size);

    @GET
    @Path("certs/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CertData getCert(@PathParam("id") CertId id);

}
