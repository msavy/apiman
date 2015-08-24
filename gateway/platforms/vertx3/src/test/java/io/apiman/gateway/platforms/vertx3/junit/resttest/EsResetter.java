package io.apiman.gateway.platforms.vertx3.junit.resttest;

import java.util.concurrent.CountDownLatch;

import io.apiman.gateway.engine.es.AbstractESComponent;
import io.apiman.gateway.engine.es.ESConstants;
import io.apiman.gateway.platforms.vertx3.config.VertxEngineConfig;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Delete;

public class EsResetter extends AbstractESComponent implements Resetter {

    public EsResetter(VertxEngineConfig vertxConf) {
        super(vertxConf.getRegistryConfig());
    }

    @Override
    public void reset() {
        try {

            CountDownLatch latch = new CountDownLatch(1);

            getClient().executeAsync(new Delete.Builder(ESConstants.INDEX_NAME).build(),
                    new JestResultHandler<JestResult>() {

                @Override
                public void completed(JestResult result) {
                    latch.countDown();
                    System.out.println("=== Deleted index " + result.getJsonString());
                }

                @Override
                public void failed(Exception ex) {
                    latch.countDown();
                    System.out.println("=== Failed index :(");
                    ex.printStackTrace();
                }
            });

            latch.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
