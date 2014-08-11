package org.overlord.apiman.gateway;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngineConfig;
import org.overlord.apiman.rt.engine.IServiceConnector;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConnectorException;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.logging.Logger;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;

public class HttpConnectorFactory implements IConnectorFactory {
    
    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<String>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("Content-Length"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("X-API-Key"); //$NON-NLS-1$
    }

    private Vertx vertx;
    private Logger log;
    private IEngineConfig config;
    
    public HttpConnectorFactory() {
        
    }
    
    public HttpConnectorFactory(Vertx vertx, Logger log, IEngineConfig config) {
        this.config = config;
        this.vertx = vertx;
        this.log = log;
    }

    @Override
    public IServiceConnector createConnector(ServiceRequest request, final Service service) {
        return new IServiceConnector() {

            @Override
            public void invoke(ServiceRequest request, IAsyncHandler<ServiceResponse> handler)
                    throws ConnectorException {
                String endpoint = StringUtils.chomp(service.getEndpoint(), "/");
                endpoint += request.getDestination();

                //FIXME use config hostname binding.
                final HttpClient client = vertx.createHttpClient().setHost("localhost").setPort(config.getServerPort()); 
                final HttpClientRequest vClientRequest = client.request(request.getType(), endpoint, new Handler<HttpClientResponse>() {
                
                    public void handle(HttpClientResponse vResponse) {
                        log.info("I should be handling the response here");   
                    }
                    
                });
                writeClientRequest(vClientRequest, request);
            }   
        };
    }
    
    private void writeClientRequest(HttpClientRequest req, ServiceRequest aRequest) {      
        req.headers().set(aRequest.getHeaders());
        req.setChunked(true);

         if(aRequest.getBody() != null) {
             try {
                 //TODO change ServiceResponse to better handle large file async.
                 byte[] buffer = new byte[1024];

                 while(aRequest.getBody().read(buffer) <= 0) {
                     req.write(new Buffer(buffer));
                 }
             } catch (IOException e) {
                 //writeError(e);
             } finally {
                 IOUtils.closeQuietly(aRequest.getBody());
             }
         }       
         req.end();
     }
}