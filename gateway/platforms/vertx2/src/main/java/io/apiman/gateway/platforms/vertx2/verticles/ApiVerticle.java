package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.api.RouteBuilder;
import io.vertx.ext.web.Router;

public class ApiVerticle extends ApimanVerticleBase {

    private RouteBuilder applicationResource;
    private RouteBuilder serviceResource;
    private RouteBuilder systemResource;

    private Router router;

    @Override
    public void start() {
        applicationResource.buildRoutes(router);
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.API;
    }
}
