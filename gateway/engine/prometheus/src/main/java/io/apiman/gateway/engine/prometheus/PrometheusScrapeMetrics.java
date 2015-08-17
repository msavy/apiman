package io.apiman.gateway.engine.prometheus;

import java.util.Map;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;

public class PrometheusScrapeMetrics implements IMetrics {

    private Map<String, String> config;
    private IComponentRegistry registry;

    public PrometheusScrapeMetrics(Map<String, String> config) {
        this.config = config;
    }

    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void record(RequestMetric metric) {

    }
}
