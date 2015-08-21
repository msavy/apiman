package io.apiman.gateway.platforms.vertx3.components;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.components.http.IHttpServerRequest;
import io.apiman.gateway.engine.components.http.IHttpServerResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.platforms.vertx3.http.HttpServiceFactory;
import io.apiman.gateway.platforms.vertx3.io.VertxApimanBuffer;
import io.apiman.gateway.platforms.vertx3.io.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx3.io.VertxServiceResponse;
import io.vertx.core.Handler;
import io.vertx.core.VoidHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

class HttpServerRequestImpl implements IHttpServerRequest, IHttpServerResponse {

    private HttpServerRequest request;
    private HttpServerResponse response;
    private VertxServiceRequest apimanRequest;

    public HttpServerRequestImpl(HttpServerRequest request) {
        this.request = request;
        this.apimanRequest = HttpServiceFactory.buildRequest(request, false);
        this.response = request.response();
    }

    @Override
    public void transmit() {
        request.resume();
    }

    @Override
    public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
        request.bodyHandler(new Handler<Buffer>() {

            @Override
            public void handle(Buffer buffer) {
                bodyHandler.handle(new VertxApimanBuffer(buffer));
            }
        });
    }

    @Override
    public void endHandler(IAsyncHandler<Void> endHandler) {
        request.endHandler(new VoidHandler() {

            @Override
            protected void handle() {
                endHandler.handle((Void) null);
            }
        });
    }

    @Override
    public ServiceRequest getHead() {
        return apimanRequest;
    }

    @Override
    public boolean isFinished() {
        return request.isEnded() && response.closed();
    }

    @Override
    public void abort() {
        if (!response.closed())
            response.close();
    }

    @Override
    public IHttpServerResponse response() {
        return this;
    }

    // Response-related stuff
    @Override
    public void write(IApimanBuffer chunk) {
        response.write((Buffer) chunk.getNativeBuffer());
    }

    @Override
    public void end() {
        response.end();
    }

    @Override
    public void setHead(ServiceResponse apimanResponse) {
        HttpServiceFactory.buildResponse(response, new VertxServiceResponse(apimanResponse));
    }

}