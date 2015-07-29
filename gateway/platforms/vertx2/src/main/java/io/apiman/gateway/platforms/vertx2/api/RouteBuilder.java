package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.common.util.SimpleStringUtils;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import org.eclipse.jetty.http.HttpStatus;

@SuppressWarnings("nls")
public interface RouteBuilder {

    void buildRoutes(Router router);

    String getPath();

    static String join(String... args) {
        return ":" + SimpleStringUtils.join("/:", args);
    }

    default String buildPath(String path) {
        return "/" + (path.length() == 0 ? getPath() : getPath() + "/" + path);
    }

    default <T> void error(RoutingContext context, int code, String message, T object) {
        HttpServerResponse response = context.response();
        response.setStatusCode(code);

        if (message != null)
            response.setStatusMessage(message);

        if(object != null)
            response.setChunked(true).write(Json.encode(object), "UTF-8");

        response.end();
    }

    default <T> void writeBody(RoutingContext context, T object) {
        context.response().putHeader("Content-Type", "application/json")
            .setChunked(true)
            .setStatusCode(HttpStatus.OK_200)
            .end(Json.encode(object), "UTF-8");
    }

    default void end(RoutingContext context, int statusCode) {
        context.response().setStatusCode(statusCode).end();
    }


    public static void main(String... args) {
        System.out.println("delete "+ join("organizationId", "applicationId", "version"));
        System.out.println(join("organizationId", "applicationId", "version"));
    }
}
