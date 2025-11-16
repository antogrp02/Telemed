/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.ParametriDAO;
import dao.RiskDAO;
import dao.AlertDAO;
import model.Parametri;
import model.Risk;
import model.Alert;
import utils.PlumberClient;
import utils.RiskEvaluator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

@WebServlet("/api/insertParameters")
public class InsertParametersServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        try {
            long idPaz = Long.parseLong(req.getParameter("id_paz"));

            Parametri p = new Parametri();
            p.setIdPaz(idPaz);
            p.setData(Timestamp.from(Instant.now()));

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

            // 1) salva parametri
            ParametriDAO.insert(p);

            // 2) calcola risk_score (ora dummy, in futuro via Plumber)
            float riskScore = PlumberClient.getRiskScore(p);

            // 3) salva in risk
            Risk r = new Risk();
            r.setIdPaz(idPaz);
            r.setData(p.getData());
            r.setRiskScore(riskScore);
            RiskDAO.insert(r);

            // 4) se > soglia, crea alert
            if (RiskEvaluator.isAlert(riskScore)) {
                Alert a = new Alert();
                a.setIdPaz(idPaz);
                a.setData(p.getData());
                a.setRiskScore(riskScore);
                a.setSoglia(RiskEvaluator.SOGLIA_ALERT);
                a.setStato("attivo");
                AlertDAO.insert(a);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"ok\",\"risk_score\":" + riskScore + "}");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"error\",\"msg\":\"" + e.getMessage() + "\"}");
        }
    }
}
