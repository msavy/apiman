/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.platforms.vertx3.junit.resttest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

import org.codehaus.jackson.JsonNode;

import io.apiman.common.util.ReflectionUtils;
import io.apiman.gateway.platforms.vertx3.verticles.InitVerticle;
import io.apiman.test.common.echo.EchoServer;
import io.apiman.test.common.resttest.IGatewayTestServer;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * A Vert.x 3 version of the gateway test server
 */
@SuppressWarnings("nls")
public class Vertx3GatewayTestServer implements IGatewayTestServer {

    protected static final int API_PORT = 8081;
    protected static final int GW_PORT = 8082;
    protected static final int ECHO_PORT = 7654;

    private EchoServer echoServer = new EchoServer(ECHO_PORT);
    private String conf;
    private CountDownLatch startLatch;
    private CountDownLatch stopLatch;
    private Resetter resetter;
    private Vertx vertx;

    /**
     * Constructor.
     */
    public Vertx3GatewayTestServer() {
    }

    @Override
    public void configure(JsonNode config) {
        ClassLoader classLoader = getClass().getClassLoader();
        String fPath = config.get("config").asText();
        resetter = getResetter(config.get("resetter").asText());
        File file = new File(classLoader.getResource(fPath).getFile());
        try {
            conf = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getApiEndpoint() {
        return "http://localhost:" + API_PORT;
    }

    @Override
    public String getGatewayEndpoint() {
        return "http://localhost:" + GW_PORT;
    }

    @Override
    public String getEchoTestEndpoint() {
        return "http://localhost:" + ECHO_PORT;
    }

    @Override
    public void start() {
        try {
            vertx = Vertx.vertx();
            echoServer.start();

            startLatch = new CountDownLatch(1);

            DeploymentOptions options = new DeploymentOptions();
            options.setConfig(new JsonObject(conf));

            vertx.deployVerticle(InitVerticle.class.getCanonicalName(),
                    options, new Handler<AsyncResult<String>>() {

                @Override
                public void handle(AsyncResult<String> event) {
                    System.out.println("Deployed init verticle!");
                    startLatch.countDown();
                }
            });

            startLatch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            stopLatch = new CountDownLatch(1);
            echoServer.stop();

            vertx.close(result -> {
                stopLatch.countDown();
            });

            stopLatch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Resetter getResetter(String name) {
        Class<Resetter> c = (Class<Resetter>) ReflectionUtils.<Resetter>loadClass(name);
        try {
            return c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
