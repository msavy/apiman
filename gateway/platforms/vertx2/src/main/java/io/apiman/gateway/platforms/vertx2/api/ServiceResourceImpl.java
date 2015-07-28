package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.gateway.api.rest.contract.IServiceResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceEndpoint;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import org.eclipse.jetty.http.HttpStatus;

public class ServiceResourceImpl extends RestResource implements IServiceResource, RouteBuilder {
    private static final String ORG_ID = "organizationId"; //$NON-NLS-1$
    private static final String SVC_ID = "serviceId"; //$NON-NLS-1$
    private static final String VER = "version"; //$NON-NLS-1$
    private static final String PUBLISH = "publish";
    private static final String RETIRE = RouteBuilder.join("retire", ORG_ID, SVC_ID, VER);

    @Override
    public void publish(Service service) throws PublishingException, NotAuthorizedException {

    }

    public void publish(RoutingContext routingContext) {
        try {
            publish(Json.decodeValue(routingContext.getBodyAsString(), Service.class));
        } catch (PublishingException e) {
            error(routingContext, HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpStatus.UNAUTHORIZED_401, e.getMessage(), e);
        }
    }

    @Override
    public void retire(String organizationId, String serviceId, String version) throws RegistrationException,
            NotAuthorizedException {
    }

    public void retire(RoutingContext routingContext) {
        String orgId = routingContext.request().getParam(ORG_ID);
        String svcId = routingContext.request().getParam(SVC_ID);
        String ver = routingContext.request().getParam(VER);

        try {
            retire(orgId, svcId, ver);
        } catch (RegistrationException e) {
            error(routingContext, HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpStatus.UNAUTHORIZED_401, e.getMessage(), e);
        }
    }

    @Override
    public ServiceEndpoint getServiceEndpoint(String organizationId, String serviceId, String version)
            throws NotAuthorizedException {
        // TODO Auto-generated method stub
        return null;
    }

    public String path() {
        return "services";
    }

    @Override
    public void buildRoutes(Router router) {
        router.put(buildPath(PUBLISH)).handler(this::publish);
        router.put(buildPath(RETIRE)).handler(this::retire);
    }

    @Override
    public String getPath() {
        // TODO Auto-generated method stub
        return null;
    }
}
