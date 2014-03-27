package org.jboss.certificate.tracker.client.dogtag;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.ClientResponseType;

@Path("")
public interface CertResource {

    @GET
    @Path("certs")
    @ClientResponseType(entityType=CertDataInfos.class)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response listCerts(
            @QueryParam("status") String status,
            @QueryParam("maxResults") Integer maxResults,
            @QueryParam("maxTime") Integer maxTime,
            @QueryParam("start") Integer start,
            @QueryParam("size") Integer size);
   
    @GET
    @Path("certs/{id}")
    @ClientResponseType(entityType=CertData.class)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCert(@PathParam("id") CertId id);

}
