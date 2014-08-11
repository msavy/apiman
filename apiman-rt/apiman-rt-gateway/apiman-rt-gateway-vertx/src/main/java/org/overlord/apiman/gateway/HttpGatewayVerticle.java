package org.overlord.apiman.gateway;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.overlord.apiman.rt.engine.EngineFactory;
import org.overlord.apiman.rt.engine.EngineResult;
import org.overlord.apiman.rt.engine.IEngine;
import org.overlord.apiman.rt.engine.IEngineConfig;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.async.IAsyncResult;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;

/**
 * HTTP Gateway Verticle
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class HttpGatewayVerticle extends Verticle {
    private Logger log;
    private IEngine engine;
    private IEngineConfig config;
    
    public void start() {     
        config = new VertxEngineConfig(container.config());
        EngineFactory factory = new EngineFactory(config);
        engine = factory.createEngine();
        log = container.logger();
        
        listen();
    }
    
    private void listen() {
        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest req) {
                
                req.bodyHandler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer body) {
                       doGateway(req, body);
                    }
    
                });
            }
        }).listen(config.getServerPort());  
    }
    
    //TODO better name...
    private void doGateway(final HttpServerRequest req, Buffer body) {
        ServiceRequest aReq = buildRequest(req, body);

        engine.execute(aReq, new IAsyncHandler<EngineResult>() {

            @Override
            public void handle(IAsyncResult<EngineResult> asyncResult) {
                
                if(asyncResult.isSuccess()) {  
                    handleResponse(req, asyncResult.getResult());   
                } else {
                    writeError(asyncResult.getError());
                }
            }

        });
    }
    
    private void handleResponse(HttpServerRequest req, EngineResult result) {
        if(result.isResponse()) {
            writeResponse(req, result.getServiceResponse());
        } else {
            writeFailure(req, result.getPolicyFailure());
        }
    }
    
    private void writeResponse(HttpServerRequest req, ServiceResponse aResponse) {      
        req.response().setStatusCode(aResponse.getCode());
        req.response().headers().set(aResponse.getHeaders());
        req.response().setChunked(true);

        if(aResponse.getBody() != null) {
            try {
                //TODO change ServiceResponse to better handle large file async.
                byte[] buffer = new byte[1024];

                while(aResponse.getBody().read(buffer) <= 0) {
                    req.response().write(new Buffer(buffer));
                }
            } catch (IOException e) {
                writeError(e);
            } finally {
                IOUtils.closeQuietly(aResponse.getBody());
            }
        }  
        
        req.response().end();
    }
    
    private void writeFailure(HttpServerRequest req, PolicyFailure result) {
        log.debug("Policy failed, but we don't actually do anything very intelligent yet."); 
        vertx.eventBus().publish("com.apiman.policyfailure", result.toString()); //FIXME JSON representing failure?
    }

    //TODO decide sensible strategy for handling Throwables.
    private void writeError(Throwable error) {
        log.error(error.toString() + "\n" + error.getStackTrace());
    }
    
    private ServiceRequest buildRequest(HttpServerRequest req, Buffer body) {
        ServiceRequest apimanRequest = new ServiceRequest();
        
        apimanRequest.setApiKey(parseApiKey(req));
        parseHeaders(apimanRequest, req);
        apimanRequest.setDestination(req.path());
        apimanRequest.setRawRequest(req);
        apimanRequest.setType(req.method());
        apimanRequest.setBody(repackageBody(body)); //TODO this is likely to cause severe performance problems.  
        
        return apimanRequest;
    }
    
    private InputStream repackageBody(Buffer body) {
        return new ByteArrayInputStream(body.getBytes());
    }
    
    private void parseHeaders(ServiceRequest aReq, HttpServerRequest vReq) { 
        for (Map.Entry<String, String> entry : vReq.headers()) {
            aReq.getHeaders().put(entry.getKey(), entry.getValue());
        } 
    }
    
    private String parseApiKey(HttpServerRequest req) {
        String headerKey = req.headers().get("X-API-Key"); //$NON-NLS-1$
        if (headerKey == null || headerKey.trim().length() == 0) {
            headerKey = parseApiKeyFromQuery(req);
        }
        return headerKey;   
    }
    
    private String parseApiKeyFromQuery(HttpServerRequest req) {
        String queryString = req.query();
        
        int idx = queryString.indexOf("apikey="); //$NON-NLS-1$
        if (idx >= 0) {
            int endIdx = queryString.indexOf('&', idx);
            if (endIdx == -1) {
                endIdx = queryString.length();
            }
            return queryString.substring(idx + 7, endIdx);
        } else {
            return null;
        }
    }
}
