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
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class ApiProcessor implements Handler<JsonObject> {
    private Set<WrappedApi> wrappedApis = new LinkedHashSet<>();
    private Set<Api> apis = null;
    private boolean first = true;
    private boolean localCanonical = true;
    private HttpClient httpClient;
    private URI endpoint;
    private Auth auth;

    public ApiProcessor(HttpClient httpClient, JsonObject globalConfig, Auth authInfo) {
        this.httpClient = httpClient;
        String api = globalConfig.getString("api");
        Objects.requireNonNull("Must provide 'api' URL", api);
        this.endpoint = URI.create(api);
        this.auth = authInfo;
    }

    @Override
    public void handle(JsonObject config) {
        System.out.println("File was modified (or startup)");

        Set<WrappedApi> apisFile = parseApisFromConfig(config);
        // if (first) {
        first = false;
        // For now assume that local is canonical.
        if (localCanonical) {
            wrappedApis = apisFile;
            apis = wrappedApis.stream()
                    .map(WrappedApi::getApi)
                    .collect(Collectors.toSet());
        } else {
            // TODO Otherwise write remote to apisFile. Probably use handler?
        }
        // }

        // Compare with remote
        getRemote(remoteApis -> {
            System.out.println("Got remote apis");
            try {
                Set<WrappedApi> wrappedRemote = remoteApis.stream()
                        .map(WrappedApi::new)
                        .collect(Collectors.toSet());
                System.out.println("Execute diff");
                executeDiff(remoteApis, wrappedRemote);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void executeDiff(Set<Api> remoteSet, Set<WrappedApi> remoteSetWrapped) {
        SetView<WrappedApi> addedOrModified = Sets.difference(wrappedApis, remoteSetWrapped);
        SetView<WrappedApi> removedOrModified = Sets.difference(remoteSetWrapped, wrappedApis);
        add(addedOrModified, remoteSet);
        remove(removedOrModified, remoteSet);
        modifiy(addedOrModified, removedOrModified, remoteSet);
    }

    private void add(SetView<WrappedApi> addedOrModified, Set<Api> remote) {
        if (addedOrModified.isEmpty())
            return;

        System.out.println("add");

        Set<Api> added = addedOrModified.stream()
                .filter(api -> !remote.contains(api.getApi()))
                .map(WrappedApi::getApi)
                .collect(Collectors.toSet());
        // Add
        publish(added);
    }

    private void modifiy(SetView<WrappedApi> a, SetView<WrappedApi> b, Set<Api> remote) {
        if (a.isEmpty() && b.isEmpty())
            return;

        Set<Api> modified = Sets.intersection(a, b).stream()
                .filter(api -> remote.contains(api.getApi()))
                .map(WrappedApi::getApi)
                .collect(Collectors.toSet());
        // Retire, then publish
        retire(modified, afterCompletion -> publish(modified));
    }

    private void remove(SetView<WrappedApi> removedOrModified, Set<Api> remote) {
        if (removedOrModified.isEmpty())
            return;

        Set<Api> removed = removedOrModified.stream()
                //.map(WrappedApi::getApi)
                .filter(api -> !remote.contains(api))
                .map(WrappedApi::getApi)
                .collect(Collectors.toSet());
        // Remove
        retire(removed, completed -> { System.err.println("Removed..."); });
    }

    private void retire(Set<Api> retireApis, Handler<Void> completed) {
        // @Path("{organizationId}/{apiId}/{version}")
        List<Future> futures = new ArrayList<>();

        for (Api api : retireApis) {
            System.out.println("Retiring " + api);
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

        auth.setAuth(deleteReq);
        deleteReq.end();
        return future;
    }

    private void publish(Set<Api> publishApis) {
        for (Api api : publishApis) {
            System.out.println("Publishing " + api);
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
                System.out.println("Put fail"); // TODO do something more interesting
            }
        }).exceptionHandler(System.err::println);
        auth.setAuth(putReq);
        putReq.setChunked(true);
        putReq.write(Json.encode(api));
        putReq.end();
    }

    @SuppressWarnings("unchecked")
    private void getRemote(Handler<Set<Api>> result) {
        // @GET
        String path = String.format("/%s/apis", endpoint.getPath());
        HttpClientRequest get = httpClient.get(endpoint.getPort(), endpoint.getHost(), path, response -> {
            if ((response.statusCode() / 100) == 2) {
                System.out.println("OK get");
                response.bodyHandler(body -> {
                   System.out.println(body.toString());
                   result.handle(Json.decodeValue(body.toString(), Set.class, Api.class)); // TODO change remote interface to set?
                });
            } else {
                System.out.println(ToStringBuilder.reflectionToString(response));
                System.out.println("get fail");
            }
        }).exceptionHandler(System.err::println);
        auth.setAuth(get);
        get.end();
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

    private static final class WrappedApi {
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
            throw new RuntimeException("InvalidComparison");
        }
    }
}
