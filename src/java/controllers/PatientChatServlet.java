package controllers;

import dao.AppuntamentoDAO;
import dao.MedicoDAO;
import dao.PazienteDAO;
import model.Medico;
import model.Paziente;
import model.Appuntamento;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@WebServlet("/patient/chat")
public class PatientChatServlet extends BaseChatServlet {

    @Override
    protected int expectedRole() {
        return 0; // ruolo paziente
    }

    @Override
    protected ChatContext buildChatContext(HttpServletRequest req, HttpServletResponse resp,
                                           HttpSession session, long myUserId)
            throws ServletException {

        try {
            // Lato paziente: prendo id_paziente dalla sessione
            long idPaziente = (long) session.getAttribute("id_paziente");

            // Carico il paziente
            Paziente paz = PazienteDAO.getByIdPaziente(idPaziente);
            if (paz == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }

            // ID medico assegnato AL PAZIENTE (corretto)
            long idMedico = paz.getIdMedico();

            // Carico il medico
            Medico med = MedicoDAO.getByIdMedico(idMedico);
            if (med == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }

            // Carico appuntamenti futuri
            List<Appuntamento> appuntamenti =
                    AppuntamentoDAO.getFuturiByMedicoAndPaziente(idMedico, idPaziente);
            req.setAttribute("appuntamenti", appuntamenti);

            // Ritorno il contesto
            return new ChatContext(
                    med.getIdUtente(), // otherUserId
                    paz,
                    med,
                    "/WEB-INF/paziente/patient_chat.jsp",
                    false // il paziente NON marca i messaggi come letti
            );

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
