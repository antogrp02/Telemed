/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.ParametriDAO;
import model.Parametri;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/patient/metrics")
public class PatientMetricsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 0) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        long idPaz = (long) s.getAttribute("id_paziente");

        int days = 7;
        String daysParam = req.getParameter("days");
        if (daysParam != null) {
            try {
                days = Integer.parseInt(daysParam);
            } catch (NumberFormatException ignore) {
            }
        }

        try {
            List<Parametri> storico = ParametriDAO.getLastDays(idPaz, days);

            String json = new Gson().toJson(storico);

            // ✅ Se è richiesta AJAX → restituisci solo JSON
            String requestedWith = req.getHeader("X-Requested-With");
            if (requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest")) {
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write(json);
                return;
            }

            // ✅ Altrimenti render JSP (prima apertura pagina)
            req.setAttribute("jsonData", json);
            req.setAttribute("days", days);

            req.getRequestDispatcher("/WEB-INF/paziente/patient_metrics.jsp").forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
