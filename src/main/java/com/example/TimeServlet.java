package com.example;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private transient TemplateEngine engine;

    @Override
    public void init(ServletConfig config) {
        engine = new TemplateEngine();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(Objects.requireNonNull(getClass().getClassLoader().getResource("templates")).getPath());
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String date = "";
        String currentDate = "";
        String timezone = "timezone";
        resp.setContentType("text/html");
        String parameter = req.getParameter(timezone);

        if (parameter == null || parameter.isEmpty()) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null && cookies.length > 0) {
                date = getString(date, cookies);
            } else {
                currentDate = getDate("UTC");
            }
        } else {
            resp.addCookie(new Cookie("Date", parameter));
            currentDate = getDate(parameter);
        }

        Context last = new Context(req.getLocale(), Map.of("date", date));
        Context current = new Context(req.getLocale(), Map.of("currentDate", currentDate));

        engine.process("result", date.isEmpty() ? current : last, resp.getWriter());
        resp.getWriter().close();
    }

    private String getString(String date, Cookie[] cookies) {
        String cookie = Arrays.stream(cookies)
                .findFirst()
                .map(Cookie::getValue)
                .orElse("");

        if (!cookie.isEmpty()) {
            date = getDate(cookie);
        }
        return date;
    }

    public String getDate(String param) {
        Date actualDate = new Date();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss z")
                .withZone(ZoneId.of(param));
        return dateFormat.format(actualDate.toInstant());
    }
}
