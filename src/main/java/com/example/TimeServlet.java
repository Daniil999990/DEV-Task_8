package com.example;

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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private transient TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(getClass().getClassLoader().getResource("templates").getPath());
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        String timezoneParam = req.getParameter("timezone");

        String lastTimezone = getLastTimezoneCookieValue(req.getCookies());

        String date = "";
        String displayText = "";

        if (timezoneParam == null) {
            date = getDate("UTC");
            displayText = "UTC";
        } else if ("UTC+2".equals(timezoneParam)) {
            date = getDate("UTC+2");
            displayText = "UTC+2";
            resp.addCookie(new Cookie("lastTimezone", timezoneParam));
        } else if (lastTimezone != null) {
            date = getDate(lastTimezone);
            displayText = lastTimezone;
        } else {
            date = getDate("UTC");
            displayText = "UTC";
        }

        Context context = new Context(req.getLocale(), Map.of("date", date, "displayText", displayText));
        engine.process("result", context, resp.getWriter());
    }

    private String getLastTimezoneCookieValue(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> "lastTimezone".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public String getDate(String param) {
        ZonedDateTime actualDateTime;
        if ("UTC+2".equals(param)) {
            actualDateTime = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(2);
        } else if (param == null) {
            actualDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        } else {
            actualDateTime = ZonedDateTime.now(ZoneId.of(param));
        }

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss z");
        return dateFormat.format(actualDateTime);
    }
}