package io.apiman.gateway.engine.prometheus;

import java.util.Map;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

public class PrometheusScrapeMetrics implements IMetrics {

    private static final String APIMAN = "apiman";
    private static final String TYPE = "type";
    private static final String APPLICATION = "application";
    private static final String SERVICE_VERSION = "serviceVersion";
    private static final String SERVICE = "service";
    private static final String METHOD = "method";
    private static final String RESPONSE_CODE = "responseCode";
    private static final String FAILURE_CODE = "failureCode";
    final Map<String, String> config;
    IComponentRegistry componentRegistry;

    final CollectorRegistry collectorRegistry = new CollectorRegistry();

    // Service
    final Counter requestsCtr = Counter.build()
            .name("requests_total").help("Total requests.")
            .namespace(APIMAN)
            .labelNames(METHOD,
                    RESPONSE_CODE,
                    SERVICE,
                    SERVICE_VERSION,
                    APPLICATION)
            .register(collectorRegistry);

    final Counter errorsCtr = Counter.build()
            .name("errors_total").help("Total errors.")
            .namespace(APIMAN)
            .labelNames(METHOD,
                    RESPONSE_CODE,
                    SERVICE,
                    SERVICE_VERSION,
                    APPLICATION)
            .register(collectorRegistry);

    final Counter failureCtr = Counter.build()
            .name("policy_failures_total").help("Total policy failures.")
            .namespace(APIMAN)
            .labelNames(METHOD,
                    RESPONSE_CODE,
                    FAILURE_CODE,
                    SERVICE,
                    SERVICE_VERSION,
                    APPLICATION)
            .register(collectorRegistry);

    final Summary requestDuration = Summary.build()
            .name("request_duration_milliseconds").help("Request duration in milliseconds.")
            .namespace(APIMAN)
            .labelNames(METHOD,
                    RESPONSE_CODE,
                    SERVICE,
                    SERVICE_VERSION,
                    APPLICATION)
            .register(collectorRegistry);

    PrometheusHttpServer server;

    public PrometheusScrapeMetrics(Map<String, String> config) {
        this.config = config;
    }

    @Override
    public void setComponentRegistry(IComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
        server = new PrometheusHttpServer(config, collectorRegistry, componentRegistry);
    }

    @Override
    public void record(RequestMetric metric) {
        doRequestsCtr(requestsCtr, metric);

        if (metric.isError()) {
            doRequestsCtr(errorsCtr, metric);
        }

        if (metric.isFailure()) {
            doFailureCtr(metric);
        }
    }

    private void doFailureCtr(RequestMetric metric) {
        failureCtr.labels(metric.getMethod(),
                Integer.toString(metric.getResponseCode()),
                Integer.toString(metric.getFailureCode()),
                metric.getServiceId(),
                metric.getServiceVersion(),
                metric.getApplicationId()).inc();
    }

    protected void doRequestsCtr(Counter ctr, RequestMetric metric) {
        ctr.labels(metric.getMethod(),
                Integer.toString(metric.getResponseCode()),
                metric.getServiceId(),
                metric.getServiceVersion(),
                metric.getApplicationId()).inc();
    }

    protected void doRequestDuration(RequestMetric metric) {
        requestDuration.labels(metric.getMethod(),
                Integer.toString(metric.getResponseCode()),
                metric.getServiceId(),
                metric.getServiceVersion(),
                metric.getApplicationId()).observe(metric.getRequestDuration());
    }
}
