package controllers;

import dao.PazienteDAO;
import dao.RiskDAO;
import dao.AlertDAO;
import dao.ChatMessageDAO;

import model.Paziente;
import model.Risk;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Long idMedico = (Long) s.getAttribute("id_medico");   // PK medico
        Long idUtente = (Long) s.getAttribute("id_utente");   // id_utente del medico

        if (idMedico == null || idUtente == null) {
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

            // 5) Messaggi non letti:
            //    prima mappa (idUtentePaziente → numeroNonLetti) sulla base di id_utente medico
            Map<Long, Integer> unreadByUser = ChatMessageDAO.getUnreadMessagesByPatient(idUtente);

            //    poi converto a: idPaziente → numeroNonLetti
            Map<Long, Integer> unreadByPaz = new HashMap<>();
            for (Paziente p : pazienti) {
                long userIdPaziente = p.getIdUtente();
                int count = unreadByUser.getOrDefault(userIdPaziente, 0);
                unreadByPaz.put(p.getIdPaz(), count);
            }

            // 6) Passo tutto alla JSP
            req.setAttribute("pazienti", pazienti);
            req.setAttribute("lastRiskByPaz", lastRiskByPaz);
            req.setAttribute("hasAlert", hasAlert);
            req.setAttribute("unreadByPaz", unreadByPaz);

            req.getRequestDispatcher("/WEB-INF/medico/doctor_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
