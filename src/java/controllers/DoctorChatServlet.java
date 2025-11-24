package controllers;

import dao.MedicoDAO;
import dao.PazienteDAO;
import model.Medico;
import model.Paziente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/doctor/chat")
public class DoctorChatServlet extends BaseChatServlet {

    @Override
    protected int expectedRole() {
        return 1;
    }

    @Override
    protected ChatContext buildChatContext(HttpServletRequest req, HttpServletResponse resp,
                                           HttpSession session, long myUserId)
            throws ServletException, IOException {

        long idMedico = (long) session.getAttribute("id_medico");

        String idStr = req.getParameter("id"); // id_paziente
        if (idStr == null) {
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

        try {
            Paziente paz = PazienteDAO.getByIdPaziente(idPaziente);
            Medico med = MedicoDAO.getByIdMedico(idMedico);

            if (paz == null || med == null || paz.getIdMedico() != idMedico) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Paziente non trovato");
                return null;
            }

            return new ChatContext(
                    paz.getIdUtente(),
                    paz,
                    med,
                    "/doctor_chat.jsp",
                    true
            );

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
