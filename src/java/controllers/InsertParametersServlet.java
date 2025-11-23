package controllers;

import dao.ParametriDAO;
import dao.RiskDAO;
import dao.AlertDAO;
import dao.PazienteDAO;
import dao.DailyDAO;
import dao.QuestionariDAO;

import model.Parametri;
import model.Questionario;
import model.Risk;
import model.Alert;
import model.Paziente;

import utils.PlumberClient;
import utils.RiskEvaluator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

@WebServlet("/api/insertParameters")
public class InsertParametersServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");

        try {
            long idPaz = Long.parseLong(req.getParameter("id_paz"));

            Timestamp now = Timestamp.from(Instant.now());
            LocalDate today = now.toLocalDateTime().toLocalDate();

            // ------------------------
            // 1) Costruzione Parametri
            // ------------------------
            Parametri p = new Parametri();
            p.setIdPaz(idPaz);
            p.setData(now);

            p.setHrCurr(Double.parseDouble(req.getParameter("hr_curr")));
            p.setRhrCurr(Double.parseDouble(req.getParameter("rhr_curr")));
            p.setHrvRmssdCurr(Double.parseDouble(req.getParameter("hrv_rmssd_curr")));
            p.setSpo2Curr(Double.parseDouble(req.getParameter("spo2_curr")));
            p.setRespRateCurr(Double.parseDouble(req.getParameter("resp_rate_curr")));
            p.setBioimpCurr(Double.parseDouble(req.getParameter("bioimp_curr")));
            p.setWeightCurr(Double.parseDouble(req.getParameter("weight_curr")));
            p.setStepsCurr(Double.parseDouble(req.getParameter("steps_curr")));

            p.setHr7d(Double.parseDouble(req.getParameter("hr_7d")));
            p.setRhr7d(Double.parseDouble(req.getParameter("rhr_7d")));
            p.setHrvRmssd7d(Double.parseDouble(req.getParameter("hrv_rmssd_7d")));
            p.setSpo27d(Double.parseDouble(req.getParameter("spo2_7d")));
            p.setRespRate7d(Double.parseDouble(req.getParameter("resp_rate_7d")));
            p.setBioimp7d(Double.parseDouble(req.getParameter("bioimp_7d")));
            p.setWeight7d(Double.parseDouble(req.getParameter("weight_7d")));
            p.setSteps7d(Double.parseDouble(req.getParameter("steps_7d")));

            p.setHrBs(Double.parseDouble(req.getParameter("hr_bs")));
            p.setRhrBs(Double.parseDouble(req.getParameter("rhr_bs")));
            p.setHrvRmssdBs(Double.parseDouble(req.getParameter("hrv_rmssd_bs")));
            p.setSpo2Bs(Double.parseDouble(req.getParameter("spo2_bs")));
            p.setRespRateBs(Double.parseDouble(req.getParameter("resp_rate_bs")));
            p.setBioimpBs(Double.parseDouble(req.getParameter("bioimp_bs")));
            p.setWeightBs(Double.parseDouble(req.getParameter("weight_bs")));
            p.setStepsBs(Double.parseDouble(req.getParameter("steps_bs")));

            // ------------------------
            // 2) Salvataggio parametri
            // ------------------------
            ParametriDAO.insert(p);

            // ------------------------
            // 3) Segno parametri_ok
            // ------------------------
            DailyDAO.setParametriOk(idPaz, today);

            // Se non ho ancora il questionario → STOP
            if (!DailyDAO.canPredict(idPaz, today)) {
                resp.getWriter().write("{\"status\":\"pending_questionnaire\"}");
                return;
            }

            // ------------------------
            // 4) Recupero Questionario del giorno
            // ------------------------
            if (!QuestionariDAO.existsForDay(idPaz, today)) {
                // per sicurezza: flag e tabella questionari non coerenti
                resp.getWriter().write("{\"status\":\"pending_questionnaire\"}");
                return;
            }

            // prendo l’ultimo questionario: dato il check sopra,
            // l’ultimo è quello di oggi
            Questionario q = QuestionariDAO.getLastByPaziente(idPaz);
            if (q == null || !q.getData().toLocalDate().equals(today)) {
                // caso anomalo, gestisco come mancanza questionario
                resp.getWriter().write("{\"status\":\"pending_questionnaire\"}");
                return;
            }

            // ------------------------
            // 5) CHIAMATA AL MODELLO ML (Plumber)
            // ------------------------
            float riskScore = PlumberClient.getRiskScore(p, q);

            // ------------------------
            // 6) Salvo rischio
            // ------------------------
            Risk r = new Risk();
            r.setIdPaz(idPaz);
            r.setData(now);
            r.setRiskScore(riskScore);
            RiskDAO.insert(r);

            DailyDAO.markPredizioneFatta(idPaz, today);

            // ------------------------
            // 7) ALERT SE NECESSARIO
            // ------------------------
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

            // ------------------------
            // 8) RISPOSTA
            // ------------------------
            resp.getWriter().write(
                    "{\"status\":\"ok\",\"risk_score\":" + riskScore + "}"
            );

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(
                    "{\"status\":\"error\",\"msg\":\"" + e.getMessage() + "\"}"
            );
        }
    }
}
