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

@WebServlet("/patient/dashboard")
public class PatientDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);

        // 1) Controllo sessione e ruolo (0 = paziente)
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 0) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) Recupero id paziente dalla sessione (impostato da LoginServlet)
        Long idPaz = (Long) s.getAttribute("id_paziente");

        if (idPaz == null) {
            req.setAttribute("error", "Errore: profilo paziente non trovato.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            // 3) Recupero ultimo set di parametri
            Parametri lastParams = ParametriDAO.getLastByPatient(idPaz);

            // 4) Recupero ultimo risk score
            Risk lastRisk = RiskDAO.getLastByPatient(idPaz);

            req.setAttribute("lastParams", lastParams);
            req.setAttribute("lastRisk", lastRisk);

            // 5) Mostra dashboard
            req.getRequestDispatcher("/patient_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}

