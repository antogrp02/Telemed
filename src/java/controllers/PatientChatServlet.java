/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
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

@WebServlet("/patient/chat")
public class PatientChatServlet extends BaseChatServlet {

    @Override
    protected int expectedRole() {
        return 0;
    }

    @Override
    protected ChatContext buildChatContext(HttpServletRequest req, HttpServletResponse resp,
                                           HttpSession session, long myUserId)
            throws ServletException {

        long idPaziente = (long) session.getAttribute("id_paziente");
        long idMedico = (long) session.getAttribute("id_medico");

        try {
            Paziente paz = PazienteDAO.getByIdPaziente(idPaziente);
            Medico med = MedicoDAO.getByIdMedico(idMedico);

            if (paz == null || med == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }

            return new ChatContext(
                    med.getIdUtente(),
                    paz,
                    med,
                    "/patient_chat.jsp",
                    false
            );

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
