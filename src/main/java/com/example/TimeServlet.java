package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String timezone = req.getParameter("timezone");

        if (timezone == null || timezone.isEmpty()) {
            resp.getWriter().write(getDate("UTC"));
        } else {
            String date = getDate(timezone);
            resp.getWriter().write(date);
        }
    }

    public static String getDate(String timeZone) {
        Date actualDate = new Date();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss z")
                .withZone(ZoneId.of(timeZone));
        return dateFormat.format(actualDate.toInstant());
    }
}
