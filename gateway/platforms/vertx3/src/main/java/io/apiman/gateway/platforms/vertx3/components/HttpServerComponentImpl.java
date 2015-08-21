package io.apiman.gateway.platforms.vertx3.components;

import java.util.Map;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.components.IHttpServerComponent;
import io.apiman.gateway.engine.components.http.IHttpServerRequest;
import io.apiman.gateway.platforms.vertx3.config.VertxEngineConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

public class HttpServerComponentImpl implements IHttpServerComponent {
    private HttpServer httpServer;

    public HttpServerComponentImpl(Vertx vertx, VertxEngineConfig engineConfig, Map<String, String> componentConfig) {
        this.httpServer = vertx.createHttpServer(); // TODO Add config options
    }

    @Override
    public IHttpServerComponent requestHandler(final IAsyncHandler<IHttpServerRequest> requestHandler) {
        httpServer.requestHandler(new Handler<HttpServerRequest>() {

            @Override
            public void handle(HttpServerRequest event) {
                requestHandler.handle(new HttpServerRequestImpl(event));
            }
        });

        return this;
    }

    @Override
    public IHttpServerComponent listen(int port) {
        httpServer.listen(port);
        return this;
    }

    @Override
    public IHttpServerComponent close() {
        httpServer.close();
        return this;
    }

}
