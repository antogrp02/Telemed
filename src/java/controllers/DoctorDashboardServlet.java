/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.PazienteDAO;
import dao.RiskDAO;
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

        // 1) Controllo accesso e ruolo = 1 (medico)
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 1) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) Recupero id medico dalla sessione (impostato da LoginServlet)
        Long idMedico = (Long) s.getAttribute("id_medico");

        if (idMedico == null) {
            req.setAttribute("error", "Errore: profilo medico non trovato.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            // 3) Recupero SOLO i pazienti in carico al medico loggato
            List<Paziente> pazienti = PazienteDAO.getByIdMedico(idMedico);

            // 4) Recupero ultimo risk score per ciascun paziente
            Map<Long, Risk> lastRiskByPaz = new HashMap<>();

            for (Paziente p : pazienti) {
                Risk r = RiskDAO.getLastByPatient(p.getIdPaz());
                lastRiskByPaz.put(p.getIdPaz(), r);
            }

            // 5) Passo i dati alla JSP
            req.setAttribute("pazienti", pazienti);
            req.setAttribute("lastRiskByPaz", lastRiskByPaz);

            req.getRequestDispatcher("/doctor_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
