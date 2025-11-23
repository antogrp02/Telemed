/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.ParametriDAO;
import dao.RiskDAO;
import dao.AlertDAO;
import dao.PazienteDAO;
import dao.DailyDAO;

import model.Parametri;
import model.Risk;
import model.Alert;
import model.Paziente;

import utils.PlumberClient;
import utils.RiskEvaluator;

import dao.QuestionariDAO;
import model.Questionario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

@WebServlet("/patient/questionnaire")
public class QuestionnaireServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 0) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.getRequestDispatcher("/questionnaire.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 0) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        long idPaz = (long) s.getAttribute("id_paziente");
        LocalDate today = LocalDate.now();

        try {
            // 🔒 1) Se già compilato oggi → torno alla PAGINA DEL QUESTIONARIO con messaggio
            if (QuestionariDAO.existsForDay(idPaz, today)) {
                req.setAttribute("alreadyToday", true);
                req.getRequestDispatcher("/questionnaire.jsp").forward(req, resp);
                return;
            }

            // 🔥 2) Salvo il questionario
            Questionario q = new Questionario();
            q.setIdPaziente(idPaz);
            q.setData(Date.valueOf(today));

            q.setDispnea(Short.parseShort(req.getParameter("dispnea")));
            q.setEdema(Short.parseShort(req.getParameter("edema")));
            q.setFatica(Short.parseShort(req.getParameter("fatica")));
            q.setOrtopnea(Short.parseShort(req.getParameter("ortopnea")));
            q.setAdl(Short.parseShort(req.getParameter("adl")));
            q.setVertigini(Short.parseShort(req.getParameter("vertigini")));

            QuestionariDAO.insert(q);

            // segno che per oggi il questionario c'è
            DailyDAO.setQuestionarioOk(idPaz, today);

            try {
                if (DailyDAO.canPredict(idPaz, today)) {

                    // prendo l'ultimo set di parametri del paziente
                    Parametri p = ParametriDAO.getLastByPatient(idPaz);
                    if (p != null) {

                        float riskScore = PlumberClient.getRiskScore(p,q);

                        Risk r = new Risk();
                        r.setIdPaz(idPaz);
                        r.setData(p.getData());  // timestamp dei parametri
                        r.setRiskScore(riskScore);
                        RiskDAO.insert(r);

                        DailyDAO.markPredizioneFatta(idPaz, today);

                        if (RiskEvaluator.isAlert(riskScore)) {
                            Paziente paz = PazienteDAO.getByIdPaziente(idPaz);
                            if (paz != null) {
                                Alert a = new Alert();
                                a.setIdPaz(idPaz);
                                a.setRiskData(r.getData());
                                a.setIdMedico(paz.getIdMedico());
                                a.setMessaggio("Rischio elevato: " + Math.round(riskScore * 100) + "%");
                                AlertDAO.insert(a);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // non blocchiamo il flusso del paziente per l'errore ML
            }
            // ✅ Dopo salvataggio → vai in dashboard con flag di successo
            HttpSession session = req.getSession();
            session.setAttribute("questionnaire_ok", true);
            resp.sendRedirect(req.getContextPath() + "/patient/dashboard");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
