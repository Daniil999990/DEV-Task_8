package com.example;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.zone.ZoneRulesException;

@WebFilter("/time")
public class TimezoneValidateFilter implements jakarta.servlet.Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        jakarta.servlet.Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        String timezone = servletRequest.getParameter("timezone");
        if (timezone == null || timezone.isEmpty()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            try {
                filterChain.doFilter(servletRequest, servletResponse);
            } catch (ZoneRulesException e) {
                HttpServletResponse response = (HttpServletResponse) servletResponse;

                response.setStatus(400);
                response.getWriter().write("Invalid timezone!" + " STATUS CODE = " + response.getStatus());
            }
        }
    }

    @Override
    public void destroy() {
        jakarta.servlet.Filter.super.destroy();
    }
}