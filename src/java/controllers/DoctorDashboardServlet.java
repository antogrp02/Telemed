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

        Long idMedico = (Long) s.getAttribute("id_medico");   // PK medico
        Long idUtente = (Long) s.getAttribute("id_utente");   // id_utente del medico

        if (idMedico == null || idUtente == null) {
            req.setAttribute("error", "Errore profilo medico non trovato.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            // 2) Pazienti associati al medico
            List<Paziente> allPazienti = PazienteDAO.getByIdMedico(idMedico);

            // ORDINA ALFABETICAMENTE
            allPazienti.sort(
                    Comparator.comparing(Paziente::getCognome, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(Paziente::getNome, String.CASE_INSENSITIVE_ORDER)
            );

            // ----- PAGINAZIONE -----
            int pageSize = 15;
            int page = 1;

            try {
                page = Integer.parseInt(req.getParameter("page"));
            } catch (Exception ignored) {}

            int totalCount = allPazienti.size();
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            if (totalPages < 1) totalPages = 1;

            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;

            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalCount);

            List<Paziente> pazienti = allPazienti.subList(start, end);
            // -------------------------

            // 3) Ultimo risk score per ogni paziente
            Map<Long, Risk> lastRiskByPaz = new HashMap<>();
            for (Paziente p : pazienti) {
                Risk r = RiskDAO.getLastByPatient(p.getIdPaz());
                lastRiskByPaz.put(p.getIdPaz(), r);
            }

            // 4) Alert attivi REALI
            Map<Long, Boolean> hasAlert = new HashMap<>();
            for (Paziente p : pazienti) {
                boolean active = AlertDAO.hasActiveAlert(p.getIdPaz(), idMedico);
                hasAlert.put(p.getIdPaz(), active);
            }

            // 5) Messaggi non letti: mappa idUtentePaziente → numero non letti
            Map<Long, Integer> unreadByUser = ChatMessageDAO.getUnreadMessagesByPatient(idUtente);

            // converti a idPaziente → numero non letti
            Map<Long, Integer> unreadByPaz = new HashMap<>();
            for (Paziente p : pazienti) {
                long userIdPaziente = p.getIdUtente();
                int count = unreadByUser.getOrDefault(userIdPaziente, 0);
                unreadByPaz.put(p.getIdPaz(), count);
            }

            // ------ PASSA TUTTO ALLA JSP ------
            req.setAttribute("pazienti", pazienti);
            req.setAttribute("lastRiskByPaz", lastRiskByPaz);
            req.setAttribute("hasAlert", hasAlert);
            req.setAttribute("unreadByPaz", unreadByPaz);

            // PAGINAZIONE
            req.setAttribute("page", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalCount", totalCount);

            req.getRequestDispatcher("/WEB-INF/medico/doctor_dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
