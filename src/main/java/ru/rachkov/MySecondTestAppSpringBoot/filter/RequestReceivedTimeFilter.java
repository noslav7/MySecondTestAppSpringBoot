package ru.rachkov.MySecondTestAppSpringBoot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestReceivedTimeFilter extends OncePerRequestFilter {

    public static final String RECEIVED_AT = RequestReceivedTimeFilter.class.getName() + ".receivedAt";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        request.setAttribute(RECEIVED_AT, System.currentTimeMillis());
        filterChain.doFilter(request, response);
    }

    public static long receivedAt(HttpServletRequest request) {
        Long receivedAt = (Long) request.getAttribute(RECEIVED_AT);
        return receivedAt != null ? receivedAt : System.currentTimeMillis();
    }
}