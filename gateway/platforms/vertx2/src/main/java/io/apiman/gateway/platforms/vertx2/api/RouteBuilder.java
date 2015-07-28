package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.common.util.SimpleStringUtils;
import io.vertx.ext.web.Router;

public interface RouteBuilder {

    static String join(String action, String... args) {
        return action + "/:" + SimpleStringUtils.join("/:", args);
    }

    void buildRoutes(Router router);

    String getPath();

    public static void main(String... args) {
        System.out.println(join("delete", "organizationId", "applicationId", "version"));
    }
}
