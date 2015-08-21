package io.apiman.gateway.engine.prometheus;

import java.util.Map;

import io.apiman.gateway.engine.IComponentRegistry;
import io.prometheus.client.CollectorRegistry;

public class PrometheusHttpServer {
    private IComponentRegistry componentRegistry;
    private CollectorRegistry collectorRegistry;

    public PrometheusHttpServer(Map<String, String> config, CollectorRegistry collectorRegistry, IComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
        this.collectorRegistry = collectorRegistry;
    }

}
