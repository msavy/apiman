package io.apiman.gateway.platforms.vertx2.api;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import org.eclipse.jetty.http.HttpStatus;

public abstract class RestResource implements RouteBuilder {
    protected <T> void error(RoutingContext context, int code, String message, T object) {
        HttpServerResponse response = context.response();
        response.setStatusCode(code);

        if (message != null)
            response.setStatusMessage(message);

        if(object != null)
            response.setChunked(true).write(Json.encode(object), "UTF-8");

        response.end();
    }

    protected <T> void writeBody(RoutingContext context, T object) {
        context.response().putHeader("Content-Type", "application/json")
            .setChunked(true)
            .setStatusCode(HttpStatus.OK_200)
            .end(Json.encode(object), "UTF-8");
    }

    protected void end(RoutingContext context, int statusCode) {
        context.response().setStatusCode(statusCode).end();
    }

    protected String buildPath(String path) {
        return "/" + (path.length() == 0 ? getPath() : getPath() + "/" + path);
    }
}
