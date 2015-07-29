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

public class ServiceResourceImpl implements IServiceResource, RouteBuilder {
    private static final String ORG_ID = "organizationId"; //$NON-NLS-1$
    private static final String SVC_ID = "serviceId"; //$NON-NLS-1$
    private static final String VER = "version"; //$NON-NLS-1$
    private static final String PUBLISH = "publish"; //$NON-NLS-1$
    private static final String RETIRE = RouteBuilder.join(ORG_ID, SVC_ID, VER);
    private static final String ENDPOINT = RouteBuilder.join(ORG_ID, SVC_ID, VER) + "/endpoint"; //$NON-NLS-1$

    @Override
    public void publish(Service service) throws PublishingException, NotAuthorizedException {

    }

    public void publish(RoutingContext routingContext) {
        try {
            publish(Json.decodeValue(routingContext.getBodyAsString(), Service.class));
            end(routingContext, HttpStatus.CREATED_201);
        } catch (PublishingException e) {
            error(routingContext, HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpStatus.UNAUTHORIZED_401, e.getMessage(), e);
        }
    }

    @Override
    public void retire(String organizationId, String serviceId, String version) throws RegistrationException,
            NotAuthorizedException {
        System.out.println(organizationId);
        System.out.println(serviceId);
        System.out.println(version);
    }

    public void retire(RoutingContext routingContext) {
        String orgId = routingContext.request().getParam(ORG_ID);
        String svcId = routingContext.request().getParam(SVC_ID);
        String ver = routingContext.request().getParam(VER);

        try {
            retire(orgId, svcId, ver);
            end(routingContext, HttpStatus.NO_CONTENT_204);
        } catch (RegistrationException e) {
            error(routingContext, HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpStatus.UNAUTHORIZED_401, e.getMessage(), e);
        }
    }

    @Override
    public ServiceEndpoint getServiceEndpoint(String organizationId, String serviceId, String version)
            throws NotAuthorizedException {
        ServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.setEndpoint(String.format("http://www.example.org/%s/%s/%s", organizationId, serviceId, version)); //TODO calculate real endpoint //$NON-NLS-1$
        return endpoint;
    }

    public void getServiceEndpoint(RoutingContext routingContext) {
        String orgId = routingContext.request().getParam(ORG_ID);
        String svcId = routingContext.request().getParam(SVC_ID);
        String ver = routingContext.request().getParam(VER);

        try {
            ServiceEndpoint endpoint = getServiceEndpoint(orgId, svcId, ver);
            writeBody(routingContext, endpoint);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpStatus.UNAUTHORIZED_401, e.getMessage(), e);
        }
    }


    @Override
    public void buildRoutes(Router router) {
        router.put(buildPath(PUBLISH)).handler(this::publish);
        router.delete(buildPath(RETIRE)).handler(this::retire);
        router.get(buildPath(ENDPOINT)).handler(this::getServiceEndpoint);

        System.out.println(buildPath(PUBLISH));
        System.out.println(buildPath(RETIRE));
        System.out.println(buildPath(ENDPOINT));
    }

    @Override
    public String getPath() {
        return "services"; //$NON-NLS-1$
    }
}
