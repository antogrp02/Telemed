/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.PazienteDAO;
import dao.RiskDAO;
import dao.AlertDAO;

import model.Paziente;
import model.Risk;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.*;

@WebServlet("/doctor/dashboard")
public class DoctorDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);

        // 1) Accesso consentito solo ai medici (role = 1)
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 1) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Long idMedico = (Long) s.getAttribute("id_medico");
        if (idMedico == null) {
            req.setAttribute("error", "Errore profilo medico non trovato.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {

            // 2) Pazienti associati al medico
            List<Paziente> pazienti = PazienteDAO.getByIdMedico(idMedico);

            // 3) Ultimo risk score per ogni paziente
            Map<Long, Risk> lastRiskByPaz = new HashMap<>();
            for (Paziente p : pazienti) {
                Risk r = RiskDAO.getLastByPatient(p.getIdPaz());
                lastRiskByPaz.put(p.getIdPaz(), r);
            }

            // 4) Alert attivi REALI (da tabella alert)
            Map<Long, Boolean> hasAlert = new HashMap<>();
            for (Paziente p : pazienti) {
                boolean active = AlertDAO.hasActiveAlert(p.getIdPaz(), idMedico);
                hasAlert.put(p.getIdPaz(), active);
            }

            // 5) Passo alla JSP
            req.setAttribute("pazienti", pazienti);
            req.setAttribute("lastRiskByPaz", lastRiskByPaz);
            req.setAttribute("hasAlert", hasAlert);

            req.getRequestDispatcher("/doctor_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
