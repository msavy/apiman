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
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
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
    private boolean localCanonical = true;
    private HttpClient httpClient;
    private URI endpoint;

    public ApiProcessor(HttpClient httpClient, JsonObject globalConfig) {
        this.httpClient = httpClient;
        String api = globalConfig.getString("api");
        Objects.requireNonNull("Must provide 'api' URL", api);
        this.endpoint = URI.create(api);
    }

    public void handle(JsonObject config) {
        Set<WrappedApi> apisFile = parseApisFromConfig(config);
        if (first) {
            first = false;

            // For now assume that local is canonical.
            if (localCanonical) {
                apis = wrappedApis.stream()
                        .map(WrappedApi::getApi)
                        .collect(Collectors.toSet());
            } else {
                // TODO Otherwise write apisFile. Probably use handler?
            }

            // Compare with remote
            getRemote(remoteApis -> {
                Set<WrappedApi> wrappedRemote = remoteApis.stream()
                        .map(WrappedApi::new)
                        .collect(Collectors.toSet());
                executeDiff(wrappedRemote);
            });
        } else {
            executeDiff(apisFile);
        }
    }

    private void executeDiff(Set<WrappedApi> apisFile) {
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
            futures.add(doDelete(api));
        }

        CompositeFuture.join(futures).setHandler(result -> {
            completed.handle((Void) null); // TODO add retry mechanism? Must cope sensibly with situation where something fails.
        });
    }

    private Future doDelete(Api api) {
        String path = String.format("/%s/apis/%s/%s/%s", endpoint.getPath(), api.getOrganizationId(), api.getApiId(), api.getVersion());
        Future future = Future.future();

        HttpClientRequest deleteReq = httpClient.delete(endpoint.getPort(), endpoint.getHost(), path, response -> {
            if ((response.statusCode() / 100) == 2) {
                future.succeeded();
            } else {
                future.fail(response.statusMessage()); // TODO do something more interesting
            }
        }).exceptionHandler(future::fail);

        deleteReq.end();
        return future;
    }

    private void publish(Set<Api> modified) {
        for (Api api : apis) {
            doPut(api);
        }
    }

    private void doPut(Api api) {
        // @PUT
        String path = String.format("/%s/apis", endpoint.getPath());

        HttpClientRequest putReq = httpClient.put(endpoint.getPort(), endpoint.getHost(), path, response -> {
            if ((response.statusCode() / 100) == 2) {
                System.out.println("OK put");
            } else {
                System.out.println("Delete fail"); // TODO do something more interesting
            }
        });//.exceptionHandler(future::fail);
        putReq.write(Json.encode(api));
        putReq.end();
    }

    private void getRemote(Handler<Set<Api>> result) {
        // @GET
        String path = String.format("/%s/apis", endpoint.getPath());

        HttpClientRequest putReq = httpClient.put(endpoint.getPort(), endpoint.getHost(), path, response -> {
            if ((response.statusCode() / 100) == 2) {
                System.out.println("OK get");
                response.bodyHandler(body -> {
                   result.handle(new HashSet<>(Json.decodeValue(body.toString(), List.class))); // TODO change remote interface to set?
                });
            } else {
                System.out.println("get fail");
            }
        });//.exceptionHandler(future::fail);
        putReq.end();
    }

    private Set<WrappedApi> parseApisFromConfig(JsonObject config) {
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
