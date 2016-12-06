/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.gateway.api.standalone;

import io.apiman.gateway.engine.beans.Api;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

@SuppressWarnings("nls")
public class ApiProcessor {
    private Set<WrappedApi> wrappedApis = new LinkedHashSet<>();
    private Set<Api> apis = null;
    private boolean first = true;
    private HttpClient httpClient;
    private JsonObject globalConfig;
    private URI endpoint;

    public ApiProcessor(HttpClient httpClient, JsonObject globalConfig) {
        this.httpClient = httpClient;
        this.globalConfig = globalConfig;
        this.endpoint = URI.create(globalConfig.getString("api"));
    }

    public void handle(JsonObject config) {
        Set<WrappedApi> apisFile = getApis(config);
        if (first) {
            first = false;
            getRemote(result -> {
                apis = wrappedApis.stream()
                        .map(WrappedApi::getApi)
                        .collect(Collectors.toSet());
            });
        } else {
            doStuff(apisFile);
        }
    }

    private void doStuff(Set<WrappedApi> apisFile) {
        SetView<WrappedApi> addedOrModified = Sets.difference(apisFile, wrappedApis);
        SetView<WrappedApi> removedOrModified = Sets.difference(wrappedApis, apisFile);

        add(addedOrModified);
        remove(removedOrModified);
        modifiy(addedOrModified, removedOrModified);
    }

    private void modifiy(SetView<WrappedApi> a, SetView<WrappedApi> b) {
        Set<Api> modified = Sets.union(a, b).stream()
                .map(WrappedApi::getApi)
                .filter(apis::contains)
                .collect(Collectors.toSet());
        // Retire, then publish
        retire(modified, afterCompletion -> publish(modified));
    }

    private void remove(SetView<WrappedApi> removedOrModified) {
        Set<Api> removed = removedOrModified.stream()
                .map(WrappedApi::getApi)
                .filter(api -> !apis.contains(api))
                .collect(Collectors.toSet());
        // Remove
        retire(removed, null);
    }

    private void add(SetView<WrappedApi> addedOrModified) {
        Set<Api> added = addedOrModified.stream()
                .map(WrappedApi::getApi)
                .filter(api -> !apis.contains(api))
                .collect(Collectors.toSet());
        // Add
        publish(added);
    }

    private void retire(Set<Api> apis, Handler<Void> completed) {

        // @Path("{organizationId}/{apiId}/{version}")

        List<Future> futures = new ArrayList<>();


        for (Api api : apis) {
            String path = String.format("/%s/%s/%s", api.getOrganizationId(), api.getApiId(), api.getVersion());
            Future future = Future.future();
            HttpClientRequest deleteReq = httpClient.delete(8080, "http://localhost", );
            deleteReq.endHandler(future.completer());
            futures.add(future);
        }

        CompositeFuture.join(futures).setHandler(result -> {
            completed.handle((Void) null);
        });
    }

    private void publish(Set<Api> modified) {

    }

    private void getRemote(AsyncResultHandler<Set<WrappedApi>> result) {
        // FIXME
    }

    private Set<WrappedApi> getApis(JsonObject config) {
        return config.getJsonArray("apis").stream()
                .map(obj -> (JsonObject) obj)
                .map(JsonObject::encode)
                .map(this::toApi)
                .collect(Collectors.toSet());
    }

    private WrappedApi toApi(String json) {
        return new WrappedApi(Json.decodeValue(json, Api.class));
    }

    private static class WrappedApi {
        private Api api;

        WrappedApi(Api api) {
            this.api = api;
        }

        public Api getApi() {
            return api;
        }

        @Override
        public int hashCode() {
            // Includes *all* fields of api - rather than the default hashcode
            return HashCodeBuilder.reflectionHashCode(api, true);
        }

        @Override
        public boolean equals(Object b) {
            if (b instanceof WrappedApi) {
                return EqualsBuilder.reflectionEquals(api, ((WrappedApi) b).getApi(), true);
            }
            return false;
        }
    }
}
