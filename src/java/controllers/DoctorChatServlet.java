/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
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
            Paziente paz = PazienteDAO.getByIdPaziente(idPaziente);
            Medico med = MedicoDAO.getByIdMedico(idMedico);

            if (paz == null || med == null || paz.getIdMedico() != idMedico) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Paziente non trovato");
                return;
            }

            long otherUserId = paz.getIdUtente();

            List<ChatMessage> history = ChatMessageDAO.getHistory(idUtente, otherUserId);

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
