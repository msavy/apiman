package io.apiman.gateway.platforms.vertx2.api;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import org.eclipse.jetty.http.HttpStatus;

public abstract class RestResource implements RouteBuilder {
    protected <T> void error(RoutingContext context, int code, String message, T object) {
        context.response().setStatusCode(code);
        if (message != null)
            context.response().setStatusMessage(message);

        if(object != null)
            context.response().write(Json.encode(object), "UTF-8");

        context.response().end();
    }

    protected <T> void writeBody(RoutingContext context, T object) {
        context.response().setChunked(true);
        context.response().setStatusCode(HttpStatus.OK_200);
        context.response().write(Json.encode(object), "UTF-8");
        context.response().end();
    }

    protected String buildPath(String path) {
        return path.length() == 0 ? getPath() : getPath() + "/" + path;
    }
}
