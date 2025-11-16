/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.ParametriDAO;
import dao.RiskDAO;
import model.Parametri;
import model.Risk;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/patient/dashboard")
public class PatientDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);

        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 0) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Long idPaz = (Long) s.getAttribute("id_paziente");

        if (idPaz == null) {
            req.setAttribute("error", "Errore: profilo paziente non trovato.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            Parametri lastParams = ParametriDAO.getLastByPatient(idPaz);
            Risk lastRisk = RiskDAO.getLastByPatient(idPaz);

            // 🔥 Storico rischio ultimi 7 giorni
            List<Risk> storicoRisk = RiskDAO.getLast7Days(idPaz);
            req.setAttribute("storicoRisk", storicoRisk);

            req.setAttribute("lastParams", lastParams);
            req.setAttribute("lastRisk", lastRisk);

            req.getRequestDispatcher("/patient_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
