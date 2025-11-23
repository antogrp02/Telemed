package controllers;

import dao.ChatMessageDAO;
import dao.MedicoDAO;
import dao.PazienteDAO;
import model.ChatMessage;
import model.Medico;
import model.Paziente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/doctor/chat")
public class DoctorChatServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // -----------------------------
        // 1) Controllo sessione
        // -----------------------------
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 1) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        long idUtente = (long) s.getAttribute("id_utente");
        long idMedico = (long) s.getAttribute("id_medico");

        String idStr = req.getParameter("id"); // id_paziente
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/doctor/dashboard");
            return;
        }

        long idPaziente;
        try {
            idPaziente = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id paziente non valido");
            return;
        }

        try {
            // -----------------------------
            // 2) Carico paziente e medico
            // -----------------------------
            Paziente paz = PazienteDAO.getByIdPaziente(idPaziente);
            Medico med = MedicoDAO.getByIdMedico(idMedico);

            if (paz == null || med == null || paz.getIdMedico() != idMedico) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Paziente non trovato");
                return;
            }

            long otherUserId = paz.getIdUtente();

            // -----------------------------
            // 3) Segno come LETTI i messaggi non letti paziente → medico
            // -----------------------------
            ChatMessageDAO.segnaComeLetti(otherUserId, idUtente);

            // -----------------------------
            // 4) Carico cronologia chat aggiornata
            // -----------------------------
            List<ChatMessage> history = ChatMessageDAO.getHistory(idUtente, otherUserId);

            // -----------------------------
            // 5) Passo dati alla JSP
            // -----------------------------
            req.setAttribute("history", history);
            req.setAttribute("myUserId", idUtente);
            req.setAttribute("otherUserId", otherUserId);
            req.setAttribute("paziente", paz);
            req.setAttribute("medico", med);

            req.getRequestDispatcher("/doctor_chat.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
