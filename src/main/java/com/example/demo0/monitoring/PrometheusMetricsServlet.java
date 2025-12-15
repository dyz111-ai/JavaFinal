package com.example.demo0.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/metrics")
public class PrometheusMetricsServlet extends HttpServlet {

    private static final PrometheusMeterRegistry registry;

    static {
        // Initialize Prometheus registry
        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        
        // Register JVM metrics
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        
        // Add custom application metrics if needed
        // registry.counter("custom.metric", "tag", "value").increment();
    }

    public static MeterRegistry getRegistry() {
        return registry;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain; version=0.0.4");
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(registry.scrape());
        }
    }
}
