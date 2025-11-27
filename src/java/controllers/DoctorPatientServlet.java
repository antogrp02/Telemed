/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import com.google.gson.Gson;
import dao.PazienteDAO;
import dao.ParametriDAO;
import dao.RiskDAO;
import dao.AlertDAO;
import dao.QuestionariDAO;
import model.Paziente;
import model.Parametri;
import model.Risk;
import model.Alert;
import model.Questionario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/doctor/patient")
public class DoctorPatientServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 1) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        long idMedico = (long) s.getAttribute("id_medico");

        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/doctor/dashboard");
            return;
        }

        long idPaz;
        try {
            idPaz = Long.parseLong(idStr);
        } catch (NumberFormatException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id paziente non valido");
            return;
        }

        try {
            // 1) verifica paziente
            Paziente paz = PazienteDAO.getByIdPaziente(idPaz);
            if (paz == null || paz.getIdMedico() != idMedico) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Paziente non trovato");
                return;
            }

            // 2) parametri
            Parametri lastParams = ParametriDAO.getLastByPatient(idPaz);
            List<Parametri> storicoParam = ParametriDAO.getLastDays(idPaz, 30);
            String metricsJson = new Gson().toJson(storicoParam);

            // 3) rischio ML
            Risk lastRisk = RiskDAO.getLastByPatient(idPaz);
            List<Risk> storicoRisk = RiskDAO.getLast7Days(idPaz);

            // 4) questionario
            Questionario lastQ = QuestionariDAO.getLastByPaziente(idPaz);

            // 5) alert corretti (non usare listActive)
            List<Alert> patientAlerts = AlertDAO.getAlertsByPaziente(idPaz);

            // 6) invio tutto alla JSP
            req.setAttribute("paziente", paz);
            req.setAttribute("lastParams", lastParams);
            req.setAttribute("lastRisk", lastRisk);
            req.setAttribute("storicoRisk", storicoRisk);
            req.setAttribute("metricsJson", metricsJson);
            req.setAttribute("lastQuestionario", lastQ);
            req.setAttribute("patientAlerts", patientAlerts);

            req.getRequestDispatcher("/WEB-INF/medico/doctor_patient.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}

