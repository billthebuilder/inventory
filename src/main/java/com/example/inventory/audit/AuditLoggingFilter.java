package com.example.inventory.audit;

import com.example.inventory.messaging.PublisherService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class AuditLoggingFilter implements Filter {
    private final PublisherService publisherService;

    public AuditLoggingFilter(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String msg = String.format("%s %s at %s from %s", req.getMethod(), req.getRequestURI(), Instant.now(), req.getRemoteAddr());
        try {
            publisherService.publishAudit(msg);
        } catch (Exception ignored) {
        }
        chain.doFilter(request, response);
    }
}
