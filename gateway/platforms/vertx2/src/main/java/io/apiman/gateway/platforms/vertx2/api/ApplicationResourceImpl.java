package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.gateway.api.rest.contract.IApplicationResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import org.eclipse.jetty.http.HttpStatus;

public class ApplicationResourceImpl extends RestResource implements IApplicationResource, RouteBuilder {

    private static final String ORG_ID = "organizationId"; //$NON-NLS-1$
    private static final String APP_ID = "applicationId"; //$NON-NLS-1$
    private static final String VER = "version"; //$NON-NLS-1$
    private static final String REGISTER = "register"; //$NON-NLS-1$
    private static final String UNREGISTER = "unregister/" + RouteBuilder.join(ORG_ID, APP_ID, VER); //$NON-NLS-1$

    @Override
    public void register(Application application) throws RegistrationException, NotAuthorizedException {

    }

    public void register(RoutingContext routingContext) {
        try {
            register(Json.decodeValue(routingContext.getBodyAsString(), Application.class));
            end(routingContext, HttpStatus.NO_CONTENT_204);
        } catch (RegistrationException e) {
            error(routingContext, HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpStatus.UNAUTHORIZED_401, e.getMessage(), e);
        }
    }

    @Override
    public void unregister(String organizationId, String applicationId, String version)
            throws RegistrationException, NotAuthorizedException {
    }

    public void unregister(RoutingContext routingContext) {
        String orgId = routingContext.request().getParam(ORG_ID);
        String appId = routingContext.request().getParam(APP_ID);
        String ver = routingContext.request().getParam(VER);

        try {
            unregister(orgId, appId, ver);
            end(routingContext, HttpStatus.NO_CONTENT_204);
        } catch (RegistrationException e) {
            error(routingContext, HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpStatus.UNAUTHORIZED_401, e.getMessage(), e);
        }
    }

    @Override
    public String getPath() {
        return "applications"; //$NON-NLS-1$
    }

    @Override
    public void buildRoutes(Router router) {
        router.put(buildPath(REGISTER)).handler(this::register);
        router.delete(buildPath(UNREGISTER)).handler(this::unregister);
    }
}
