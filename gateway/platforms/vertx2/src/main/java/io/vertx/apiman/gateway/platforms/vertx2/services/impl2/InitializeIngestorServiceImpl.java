package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

public class InitializeIngestorServiceImpl implements InitializeIngestorService {

    private Vertx vertx;

    public InitializeIngestorServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void createIngestor(String uuid, Handler<AsyncResult<IngestorToPolicyService>> resultHandler) {
        System.out.println("Creating ingestor who will listen on " + uuid);

        IngestorToPolicyImpl service = new IngestorToPolicyImpl(vertx);

        ProxyHelper.registerService(IngestorToPolicyService.class,
                vertx, service, uuid);

        service.headHandler((Handler<VertxServiceRequest>) serviceRequest -> {

        });

        service.bodyHandler((Handler<VertxApimanBuffer>) body -> {

        });

        service.endHandler((Handler<Void>) voidHandler -> {

        });

        resultHandler.handle(Future.succeededFuture(service));
//        System.out.println("Writing to " + uuid + ".response");
//        PolicyToIngestorService proxy = PolicyToIngestorService.createProxy(vertx, uuid + ".response");
//        proxy.write("test-response");
    }
}
