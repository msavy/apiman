/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.engine.vertx.shareddata;

import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.platforms.vertx3.common.verticles.Json;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@SuppressWarnings("nls")
public class URILoadingRegistry extends InMemoryRegistry {
    Vertx vertx;
    IEngineConfig vxConfig;
    // Protected by DCL, use #getUriLoader
    private static volatile OneShotURILoader instance;

    // TODO: Authentication for HTTP(S).
    public URILoadingRegistry(Vertx vertx, IEngineConfig vxConfig, Map<String, String> options) {
        super();
        this.vertx = vertx;
        this.vxConfig = vxConfig;

        Arguments.require(options.containsKey("configUri"), "configUri is required in configuration");
        URI uri = URI.create(options.get("configUri"));
        getURILoader(vertx, uri).subscribe(this, result -> {});
    }

    private OneShotURILoader getURILoader(Vertx vertx, URI uri) {
        if (instance == null) {
            synchronized(URILoadingRegistry.class) {
                if (instance == null) {
                    instance = new OneShotURILoader(vertx, uri);
                }
            }
        }
        return instance;
    }

    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    protected void publishApiInternal(Api api, IAsyncResultHandler<Void> handler) {
        super.publishApi(api, handler);
    }

    protected void registerClientInternal(Client client, IAsyncResultHandler<Void> handler) {
        super.registerClient(client, handler);
    }

    private static final class OneShotURILoader {
        private Vertx vertx;
        private Deque<URILoadingRegistry> awaiting = new ArrayDeque<>();
        private boolean dataProcessed = false;
        private URI uri;
        private IAsyncResultHandler<Void> failureHandler;
        private Buffer rawData;
        private List<Client> clients;
        private List<Api> apis;

        public OneShotURILoader(Vertx vertx, URI uri) {
            this.vertx = vertx;
            this.uri = uri;
            loadData();
        }

        private void loadData() {
            switch (uri.getScheme().toLowerCase()) {
            case "http":
                fetchHttp(false);
                break;
            case "https":
                fetchHttp(true);
                break;
            case "file":
                fetchFile();
                break;
            default:
                throw new UnsupportedProtocolException(String.format("%s is not supported. Available: http, https and file.", uri.getScheme()));
            }
        }

        private void fetchFile() {
            vertx.fileSystem().readFile(uri.getPath(), result -> {
                if (result.succeeded()) {
                    rawData = result.result();
                    processData();
                } else {
                    failureHandler.handle(AsyncResultImpl.create(result.cause()));
                }
            });
        }

        private void fetchHttp(boolean isHttps) {
            vertx.createHttpClient(new HttpClientOptions().setSsl(isHttps))
                .get(uri.getPort(), uri.getHost(), uri.getPath(), clientResponse -> {
                    if (clientResponse.statusCode() / 100 == 2) {
                        clientResponse.handler(data -> {
                            if (rawData == null) {
                                rawData = data;
                            } else {
                                rawData.appendBuffer(data);
                            }
                        })
                        .endHandler(end -> processData())
                        .exceptionHandler(exception -> failureHandler.handle(AsyncResultImpl.create(exception)));
                    } else { // TODO Handle bad response code.

                    }
                }).exceptionHandler(exception -> failureHandler.handle(AsyncResultImpl.create(exception)));

        }

        @SuppressWarnings("unchecked")
        private void processData() {
            // TODO more robust checking and handling.
            JsonObject json = new JsonObject();
            clients = Json.decodeValue(json.getJsonObject("clients").encode(), List.class, Client.class);
            apis = Json.decodeValue(json.getJsonObject("apis").encode(), List.class, Api.class);
            dataProcessed = true;
            checkQueue();
        }

        public synchronized void subscribe(URILoadingRegistry urlLoadingRegistry, IAsyncResultHandler<Void> failureHandler) {
            Objects.requireNonNull(urlLoadingRegistry, "no null registry allowed.");
            this.failureHandler = failureHandler;
            awaiting.add(urlLoadingRegistry);
            vertx.runOnContext(action -> checkQueue());
        }

        private void checkQueue() {
            if (dataProcessed && awaiting.size()>0) {
                loadDataIntoRegistries();
            }
        }

        private void loadDataIntoRegistries() {
            URILoadingRegistry reg = null;
            while ((reg = awaiting.poll()) != null) {
                for (Api api : apis) {
                    reg.publishApiInternal(api, handleAnyFailure());
                }
                for (Client client : clients) {
                    reg.registerClientInternal(client, handleAnyFailure());
                }
            }
        }

        private IAsyncResultHandler<Void> handleAnyFailure() {
            return result -> {
                if (result.isError()) {
                    failureHandler.handle(AsyncResultImpl.create(result.getError()));
                    throw new RuntimeException(result.getError());
                }
            };
        }
    }

}
