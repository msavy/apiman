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
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.LinkedHashSet;
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

    public ApiProcessor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void handle(JsonObject config) {
        Set<WrappedApi> apisFile = getApis(config);

        if (first) {
            wrappedApis = getRemote();
            apis = wrappedApis.stream()
                    .map(WrappedApi::getApi)
                    .collect(Collectors.toSet());
            first = false;
        }

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

        // retire, then publish
        retire(modified);
        publish(modified);
    }

    private void remove(SetView<WrappedApi> removedOrModified) {
        Set<Api> removed = removedOrModified.stream()
                .map(WrappedApi::getApi)
                .filter(api -> !apis.contains(api))
                .collect(Collectors.toSet());

        // Remove
        retire(removed);
    }

    private void add(SetView<WrappedApi> addedOrModified) {
        Set<Api> added = addedOrModified.stream()
                .map(WrappedApi::getApi)
                .filter(api -> !apis.contains(api))
                .collect(Collectors.toSet());
        // Add
        publish(added);
    }


    private void retire(Set<Api> modified) {
//        httpClient.delete(8080, "http://localhost", "/apiman-gateway-api", result -> {
//        });

//        @SuppressWarnings("rawtypes")
//        List<Future> futures = new ArrayList<>(modified.size());
//
//        for (int i = 0; i < modified.size(); i++) {
//            futures.add(Future.future());
//        }
//
//        httpClient.delete(8080, "http://localhost", "/apiman-gateway-api", null);
//
//        CompositeFuture.all(futures);

          HttpClientRequest deleteReq = httpClient.delete(8080, "http://localhost", "/apiman-gateway-api", result -> {

          });

    }

    private void publish(Set<Api> modified) {

    }

    private Set<WrappedApi> getRemote() {
        return Collections.EMPTY_SET;
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
