package io.apiman.gateway.platforms.vertx3.junit.resttest;

import java.io.IOException;
import java.util.Map;

import io.apiman.gateway.engine.es.AbstractESComponent;
import io.apiman.gateway.engine.es.ESConstants;
import io.searchbox.core.Delete;

public class EsResetter extends AbstractESComponent implements Resetter {

    public EsResetter(Map<String, String> config) {
        super(config);
    }

    @Override
    public void reset() {
        try {
            getClient().execute(new Delete.Builder(ESConstants.INDEX_NAME).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
