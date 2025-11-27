/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.AlertDAO;
import dao.PazienteDAO;
import model.Paziente;
import java.util.Map;
import java.util.HashMap;
import model.Alert;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/doctor/alerts")
public class DoctorAlertsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 1) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            long idMedico = (long) s.getAttribute("id_medico");
            List<Alert> alerts = AlertDAO.getAlertsByMedico(idMedico);
            Map<Long, Paziente> pazById = new HashMap<>();
            for (Alert a : alerts) {
                Paziente p = PazienteDAO.getByIdPaziente(a.getIdPaz());
                pazById.put(a.getIdPaz(), p);
            }

            req.setAttribute("alerts", alerts);
            req.setAttribute("pazienti", pazById);
            req.setAttribute("alerts", alerts);
            req.getRequestDispatcher("/WEB-INF/medico/doctor_alerts.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
