package com.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss z");

    private transient TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(Thread.currentThread().getContextClassLoader().getResource("templates/").getPath());
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        String timezoneParam = req.getParameter("timezone");

        String lastTimezone = getLastTimezoneCookieValue(req.getCookies());

        String date;
        String displayText;

        try {
            if (timezoneParam == null) {
                if (lastTimezone != null && isValidTimezone(lastTimezone)) {
                    date = getDate(lastTimezone);
                    displayText = lastTimezone;
                } else {
                    date = getDate("UTC");
                    displayText = "UTC";
                }
            } else if (isValidTimezone(timezoneParam)) {
                date = getDate(timezoneParam);
                displayText = timezoneParam;
                resp.addCookie(new Cookie("lastTimezone", timezoneParam));
            } else {
                date = getDate("UTC");
                displayText = "UTC";
            }
        } catch (Exception e) {
            date = getDate("UTC");
            displayText = "UTC";
        }

        Context context = new Context(req.getLocale());
        context.setVariable("date", date);
        context.setVariable("displayText", displayText);

        String templatePath = "result"; // Шлях до шаблону в ресурсах
        engine.process(templatePath, context, resp.getWriter());
    }

    private String getLastTimezoneCookieValue(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "lastTimezone".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public String getDate(String param) {
        try {
            if (isValidTimezone(param)) {
                return dateFormat.format(ZonedDateTime.now(ZoneId.of(param)).withZoneSameInstant(ZoneId.of("UTC")));
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}