package controllers;

import dao.AppuntamentoDAO;
import dao.PazienteDAO;
import model.Appuntamento;
import model.Paziente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/doctor/appointments")
public class DoctorAppointmentsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DoctorAppointmentsServlet.class.getName());
    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);

        // Accesso consentito solo ai medici (role = 1)
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 1) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Long idMedico = (Long) s.getAttribute("id_medico");
        if (idMedico == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int page = 1;
        String pageParam = req.getParameter("page");
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException ex) {
                page = 1;
            }
        }
        if (page < 1) {
            page = 1;
        }

        try {
            int totalCount = AppuntamentoDAO.countFuturiByMedico(idMedico);
            int totalPages = (int) Math.ceil(totalCount / (double) PAGE_SIZE);
            if (totalPages == 0) {
                totalPages = 1;
            }
            if (page > totalPages) {
                page = totalPages;
            }

            List<Appuntamento> appuntamenti = AppuntamentoDAO.getFuturiByMedico(idMedico, page, PAGE_SIZE);

            // Mappa id_paziente -> Paziente per nome/cognome/CF
            Map<Long, Paziente> pazById = new HashMap<>();
            for (Appuntamento a : appuntamenti) {
                long idPaz = a.getIdPaziente();
                if (!pazById.containsKey(idPaz)) {
                    Paziente p = PazienteDAO.getByIdPaziente(idPaz);
                    pazById.put(idPaz, p);
                }
            }

            LOGGER.log(Level.INFO,
                    "DoctorAppointmentsServlet.doGet - medico={0}, page={1}, totalCount={2}, totalPages={3}",
                    new Object[]{idMedico, page, totalCount, totalPages});

            req.setAttribute("appuntamenti", appuntamenti);
            req.setAttribute("pazById", pazById);
            req.setAttribute("page", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalCount", totalCount);

            req.getRequestDispatcher("/WEB-INF/medico/doctor_appointments.jsp").forward(req, resp);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento degli appuntamenti per il medico " + idMedico, e);
            throw new ServletException("Errore nel caricamento degli appuntamenti", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);

        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 1) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Long idMedico = (Long) s.getAttribute("id_medico");
        if (idMedico == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        String idStr = req.getParameter("id");
        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ex) {
                page = 1;
            }
        }

        if (action == null || idStr == null || action.isBlank() || idStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/doctor/appointments?page=" + page);
            return;
        }

        try {
            long idApp = Long.parseLong(idStr);

            if ("cancel".equals(action)) {
                AppuntamentoDAO.delete(idApp);
                LOGGER.log(Level.INFO,
                        "DoctorAppointmentsServlet.doPost - medico={0} ha ANNULLATO l''appuntamento id={1}",
                        new Object[]{idMedico, idApp});
            } else if ("reschedule".equals(action)) {
                String dataStr = req.getParameter("data");
                String oraStr = req.getParameter("ora");

                if (dataStr == null || oraStr == null
                        || dataStr.isBlank() || oraStr.isBlank()) {
                    resp.sendRedirect(req.getContextPath() + "/doctor/appointments?page=" + page);
                    return;
                }

                LocalDate data = LocalDate.parse(dataStr);
                LocalTime ora = LocalTime.parse(oraStr);
                LocalDateTime ldt = LocalDateTime.of(data, ora);
                Timestamp ts = Timestamp.valueOf(ldt);

                AppuntamentoDAO.updateDataOra(idApp, ts);

                LOGGER.log(Level.INFO,
                        "DoctorAppointmentsServlet.doPost - medico={0} ha RINVIATO l''appuntamento id={1} a {2}",
                        new Object[]{idMedico, idApp, ts});
            }

            resp.sendRedirect(req.getContextPath() + "/doctor/appointments?page=" + page);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Errore nella gestione dell''azione '" + action + "' sugli appuntamenti per il medico " + idMedico,
                    e);
            throw new ServletException("Errore nella modifica dell'appuntamento", e);
        }
    }
}
