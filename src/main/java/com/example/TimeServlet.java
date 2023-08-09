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
import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
        String timezone = "timezone";
        resp.setContentType("text/html");
        String parameter = req.getParameter(timezone);

        String lastTimezone = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lastTimezone")) {
                    lastTimezone = cookie.getValue();
                    break;
                }
            }
        }

        if (parameter == null || parameter.isEmpty()) {
            if (lastTimezone != null) {
                date = getDate(lastTimezone);
            } else {
                date = getDate("UTC");
            }
        } else {
            String encodedParameter = URLEncoder.encode(parameter, "UTF-8");
            resp.addCookie(new Cookie("lastTimezone", parameter));
            if (lastTimezone != null) {
                date = getDate(parameter);
            } else {
                date = getDate("UTC");
            }
        }


        Context context = new Context(req.getLocale(), Map.of("date", date));
        engine.process("result", context, resp.getWriter());
    }

    public String getDate(String param) {
        ZonedDateTime actualDateTime;
        if (param == null) {
            actualDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
        } else {
            actualDateTime = ZonedDateTime.now(ZoneId.of(param));
        }

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss z");
        return dateFormat.format(actualDateTime);
    }
}
