/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.gateway.api.rest.contract;

import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.swagger.annotations.ResponseHeader;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * The API API.  Ha!
 *
 * @author eric.wittmann@redhat.com
 */
@Path("apis")
@io.swagger.annotations.Api
public interface IApiResource {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void publish(Api api) throws PublishingException, NotAuthorizedException;

    @DELETE
    @Path("{organizationId}/{apiId}/{version}")
    public void retire(@PathParam("organizationId") String organizationId,
                       @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws RegistrationException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/{apiId}/{version}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiEndpoint getApiEndpoint(@PathParam("organizationId") String organizationId,
                                      @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws NotAuthorizedException;

    /**
     * Paginated list of APIs (see Link header w.r.t rfc5988)
     *
     * @return list of APIs
     * @throws NotAuthorizedException
     */
    @GET
    @Path("{organizationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Api> listApis(@PathParam("organizationId") String organizationId,
                              @QueryParam("page") int page,
                              @QueryParam("pageSize") int pageSize) throws NotAuthorizedException;


    // Probably should be simple list of
    //    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Async
//    public List<Api> listApis() throws NotAuthorizedException;


}
