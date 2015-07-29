package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.common.util.SimpleStringUtils;
import io.vertx.ext.web.Router;

public interface RouteBuilder {

    static String join(String... args) {
        return ":" + SimpleStringUtils.join("/:", args);
    }

    void buildRoutes(Router router);

    String getPath();

    public static void main(String... args) {
        System.out.println("delete "+ join("organizationId", "applicationId", "version"));
        System.out.println(join("organizationId", "applicationId", "version"));
    }
}
