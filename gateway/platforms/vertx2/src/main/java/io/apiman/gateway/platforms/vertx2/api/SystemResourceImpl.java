package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.gateway.api.rest.contract.ISystemResource;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import org.eclipse.jetty.http.HttpStatus;

public class SystemResourceImpl extends RestResource implements ISystemResource, RouteBuilder {

    private static final String STATUS = "status";

    @Override
    public SystemStatus getStatus() {
        SystemStatus status = new SystemStatus();
        status.setUp(true);
        status.setVersion("1"); // TODO do something more sensible
        return status;
    }

    public void getStatus(RoutingContext routingContext) {
        if (getStatus() == null) {
            error(routingContext, HttpStatus.INTERNAL_SERVER_ERROR_500, "Status invalid", null);
        } else {
            writeBody(routingContext, getStatus());
        }
    }

    @Override
    public void buildRoutes(Router router) {
        router.get(buildPath(STATUS)).handler(this::getStatus);
    }

    @Override
    public String getPath() {
        return "system";
    }
}
