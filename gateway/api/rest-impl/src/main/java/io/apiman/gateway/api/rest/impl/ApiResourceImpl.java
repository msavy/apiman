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

package io.apiman.gateway.api.rest.impl;

import io.apiman.gateway.api.rest.contract.IApiResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of the API API :).
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiResourceImpl extends AbstractResourceImpl implements IApiResource {

    /**
     * Constructor.
     */
    public ApiResourceImpl() {
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IApiResource#publish(io.apiman.gateway.engine.beans.Api)
     */
    @Override
    public void publish(Api api) throws PublishingException, NotAuthorizedException {
        final Set<Throwable> errorHolder = new HashSet<>();
        final CountDownLatch latch = new CountDownLatch(1);
        // Publish api; latch until result returned and evaluated
        getEngine().getRegistry().publishApi(api, latchedResultHandler(latch, errorHolder));
        awaitOnLatch(latch, errorHolder);
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IApiResource#retire(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void retire(String organizationId, String apiId, String version) throws RegistrationException,
            NotAuthorizedException {
        final Set<Throwable> errorHolder = new HashSet<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Api api = new Api();
        api.setOrganizationId(organizationId);
        api.setApiId(apiId);
        api.setVersion(version);
        // Retire api; latch until result returned and evaluated
        getEngine().getRegistry().retireApi(api, latchedResultHandler(latch, errorHolder));
        awaitOnLatch(latch, errorHolder);
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IApiResource#getApiEndpoint(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiEndpoint getApiEndpoint(String organizationId, String apiId, String version)
            throws NotAuthorizedException {
        return getPlatform().getApiEndpoint(organizationId, apiId, version);
    }
//
//    @Override
//    public List<Api> listApis(String organizationId, int index, int maxRecords) throws NotAuthorizedException {
//        getEngine().getRegistry().listApis(organizationId);
//    }


    @Override
    public List<Api> listApis(String organizationId,
                              int page,
                              int pageSize) throws NotAuthorizedException {
//        final Set<Throwable> errorHolder = new HashSet<>();
//        final CountDownLatch latch = new CountDownLatch(1);
//
//        getEngine().getRegistry().listApis(organizationId, page, pageSize, result -> {
//            if (result.isError()) {
//                errorHolder.add(result.getError());
//            }
//            latch.countDown();
//        });
//
//        awaitOnLatch(latch, errorHolder);
//        return result;
    }

}
