package com.example.userservice.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final MeterRegistry meterRegistry;

    public RequestLoggingFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (!req.getRequestURI().startsWith("/user")) {
            chain.doFilter(request, response);
            return;
        }

        // 1. Generate Request ID
        String requestId = UUID.randomUUID().toString();
        // Add to response header for client visibility
        res.addHeader("X-Request-ID", requestId);

        long startTime = System.currentTimeMillis();

        // Metrics: Total Requests
        meterRegistry.counter("total_requests").increment();

        try {
            chain.doFilter(request, response);

            // Metrics: Success/Failure based on status code
            if (res.getStatus() >= 200 && res.getStatus() < 400) {
                meterRegistry.counter("success_count").increment();
            } else {
                meterRegistry.counter("failure_count").increment();
            }

        } catch (Exception e) {
            meterRegistry.counter("failure_count").increment();
            logger.error("Request {} failed: {}", requestId, e.getMessage());
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Metrics: Latency
            meterRegistry.timer("request_latency_ms").record(duration, TimeUnit.MILLISECONDS);

            // Log: ID, IP, Latency, Status
            String clientIp = getClientIp(req);
            logger.info("RequestID: {}, UserIP: {}, Method: {}, URI: {}, Status: {}, Latency: {}ms",
                    requestId, clientIp, req.getMethod(), req.getRequestURI(), res.getStatus(), duration);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
