package io.apiman.gateway.engine.prometheus;

import java.util.Map;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;

public abstract class AbstractPrometheusMetrics implements IMetrics {

    private SimpleCollector<Gauge.Child, ? extends SimpleCollector> collector;

    public AbstractPrometheusMetrics(Map<String, String> config) {

    }

    /**
     * Records the metrics for a single request.  Most implementations will likely
     * asynchronously process this information.
     * @param metric the request metric
     */
    @Override
    public void record(RequestMetric metric);

    /**
     * Provides the component registry (before any call to {@link #record(RequestMetric)})
     * is made. Metrics can then access HTTP client components, etc.
     * @param registry the component registry
     */
    @Override
    public void setComponentRegistry(IComponentRegistry registry);

}
