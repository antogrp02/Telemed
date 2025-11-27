package controllers;

import dao.AppuntamentoDAO;
import dao.MedicoDAO;
import dao.PazienteDAO;
import model.Appuntamento;
import model.Medico;
import model.Paziente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/doctor/chat")
public class DoctorChatServlet extends BaseChatServlet {

    @Override
    protected int expectedRole() {
        return 1; // medico
    }

    @Override
    protected ChatContext buildChatContext(HttpServletRequest req,
                                           HttpServletResponse resp,
                                           HttpSession session,
                                           long myUserId)
            throws Exception {

        Long idMedico = (Long) session.getAttribute("id_medico");

        // 1) ID PAZIENTE DALLA QUERY
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/doctor/dashboard");
            return null;
        }

        long idPaziente;
        try {
            idPaziente = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id paziente non valido");
            return null;
        }

        // 2) CARICO PAZIENTE + MEDICO
        Paziente paz = PazienteDAO.getByIdPaziente(idPaziente);
        Medico med = MedicoDAO.getByIdMedico(idMedico);

        if (paz == null || med == null || paz.getIdMedico() != idMedico) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Paziente non trovato");
            return null;
        }

        // 3) CARICO APPUNTAMENTI FUTURI
        List<Appuntamento> appuntamenti =
                AppuntamentoDAO.getFuturiByMedicoAndPaziente(idMedico, idPaziente);
        req.setAttribute("appuntamenti", appuntamenti);

        // 4) RITORNO IL CONTEXT
        return new ChatContext(
                paz.getIdUtente(),   // otherUserId
                paz,
                med,
                "/WEB-INF/medico/doctor_chat.jsp",
                true                 // lato medico: segna come letti
        );
    }
}
