package com.inp.msa.inpmsaauth.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            logRequest(wrappedRequest);
            logResponse(wrappedResponse, duration);

            wrappedResponse.copyBodyToResponse();;
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((key, value) -> {
            params.append(key).append("=").append(String.join(",", value)).append("\n");
        });

        String method = request.getMethod();
        String authorizationHeader = request.getHeader("Authorization");
        String paramString = params.toString();
        if (paramString.isEmpty()) {
            paramString = "null\n";
        }

        logger.info("\n[Request]\n- method: {}\nauthorization header: {}\n- uri: {}\n- params:\n{}- body: {}",
                    method,
                    authorizationHeader != null ? authorizationHeader : "null",
                    request.getRequestURI(),
                    paramString,
                    getRequestBody(request));
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        logger.info("\n[Response]\n- status: {}\n- duration: {}ms\n- body:\n{}",
                    response.getStatus(),
                    duration,
                    getResponseBody(response));
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buffer = request.getContentAsByteArray();
        if (buffer.length == 0) {
            return "null";
        }
        return new String(buffer, 0, buffer.length, StandardCharsets.UTF_8);
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buffer = response.getContentAsByteArray();
        if (buffer.length == 0) {
            return "null";
        }
        return new String(buffer, 0, buffer.length, StandardCharsets.UTF_8);
    }
}
