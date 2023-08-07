package com.example;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private transient TemplateEngine templateEngine;

    @Override
    public void init(ServletConfig config) throws ServletException {
        templateEngine = createTemplateEngine();
    }

    private TemplateEngine createTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
        return engine;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        String timezoneParam = req.getParameter("timezone");
        String formattedDate = "";

        if (timezoneParam == null || timezoneParam.isEmpty() || !isValidTimezone(timezoneParam)) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null && cookies.length > 0) {
                formattedDate = getDateFromCookie(cookies);
            }
        } else {
            resp.addCookie(new Cookie("Date", timezoneParam));
            formattedDate = getDate(timezoneParam);
        }

        Context context = new Context(req.getLocale());
        context.setVariable("date", formattedDate);
        templateEngine.process("text", context, resp.getWriter());
    }

    private boolean isValidTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    private String getDateFromCookie(Cookie[] cookies) {
        String timezoneCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("Date"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse("");
        return !timezoneCookie.isEmpty() ? getDate(timezoneCookie) : "";
    }

    private String getDate(String param) {
        Date actualDate = new Date();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss z")
                .withZone(ZoneId.of(param));
        return dateFormat.format(actualDate.toInstant());
    }
}
