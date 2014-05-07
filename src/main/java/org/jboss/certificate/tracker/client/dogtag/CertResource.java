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
package org.jboss.certificate.tracker.client.dogtag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.ClientResponseType;

/**
 * JAX-RS interface for getting certificates from Dogtag Certificate System
 * 
 * @author Filip Bogyai
 */
@Path("")
public interface CertResource {

    /**
     * Get all information about certificates depending on search criteria
     * 
     * @param status of certificate (VALID/REVOKED)
     * @param maxResults maximal number of returned certificates
     * @param maxTime end date of certificate validity
     * @param start id number of first returned certificate  
     * @param size number of requested certificates
     */
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
   
    /**
     * Get certificate data with specified id
     * 
     * @param id number of requested certificate     
     */
    @GET
    @Path("certs/{id}")
    @ClientResponseType(entityType=CertData.class)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCert(@PathParam("id") CertId id);

}
