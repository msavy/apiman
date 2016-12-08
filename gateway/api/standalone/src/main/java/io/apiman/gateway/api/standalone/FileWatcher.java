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
import io.apiman.gateway.engine.beans.Client;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

@SuppressWarnings("nls")
public class FileWatcher {
    private Path path;
    private Vertx vertx;
    private long DEFAULT_INTERVAL = 10000;
    private long checkInterval = DEFAULT_INTERVAL;
    private byte[] previousHash = null;
    private byte[] currentHash;

    private JsonObject config;
    private Logger logger;
    private Handler<JsonObject> changeHandler;

//    private Set<Api> apis = new LinkedHashSet<>();
//    private Set<Client> clients = new LinkedHashSet<>();
//
//    private Handler<Set<Api>> newApisHandler;
//    private Handler<Set<Client>> newClientsHandler;
//    private Handler<Set<>> deletedApisHandler;
//    private Handler

    public FileWatcher(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.logger = LoggerFactory.getLogger(FileWatcher.class);

        logger.info(config);

        JsonObject fileConfig = config.getJsonObject("file");
        Objects.requireNonNull(fileConfig, "file config object is required");
        {
            path = FileSystems.getDefault().getPath(fileConfig.getString("path")); // TODO use code gen converters instead or validate somewhere centrall?
            Objects.requireNonNull(path, "Must provide config.path attribute pointing to your apiman-gateway-api-standalone file");
            checkInterval = Long.max(0, fileConfig.getLong("checkInterval", DEFAULT_INTERVAL) * 1000);
        }
        watch();
    }

    private void watch() {
        vertx.setPeriodic(checkInterval, id -> {
            if (hasFileChanged()) {
                changeHandler.handle(readFile());
            }
        });
    }

    // Initial sweep
    private JsonObject readFile() {
        try {
            return new JsonObject(new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//
//        this.apis = config.getJsonArray("apis").stream()
//            .map(obj -> (JsonObject) obj)
//            .map(JsonObject::encode)
//            .map(this::toApi)
//            .collect(Collectors.toSet());
//
//        this.clients = config.getJsonArray("clients").stream()
//                .map(obj -> (JsonObject) obj)
//                .map(JsonObject::encode)
//                .map(this::toClient)
//                .collect(Collectors.toSet());


//        checkForNonActiveEntities(apis);
//        checkForNonActiveEntities(clients);
    }

//    private static Set<Api> getNonActiveEntities(Set<Api> apis) {
//
//    }

    private Api toApi(String json) {
        return Json.decodeValue(json, Api.class);
    }

    private Client toClient(String json) {
        return Json.decodeValue(json, Client.class);
    }

    private boolean hasFileChanged() {
        currentHash = hashFile();
        boolean res = !Arrays.equals(previousHash, currentHash);
        previousHash = currentHash;
        return res;
    }

    private byte[] hashFile() {
        try {
            return DigestUtils.sha1(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the changeHandler
     */
    public Handler<JsonObject> getChangeHandler() {
        return changeHandler;
    }

    /**
     * @param changeHandler the changeHandler to set
     */
    public FileWatcher setChangeHandler(Handler<JsonObject> changeHandler) {
        this.changeHandler = changeHandler;
        return this;
    }
}
