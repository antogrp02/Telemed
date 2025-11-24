/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.AppuntamentoDAO;
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
import model.Appuntamento;

@WebServlet("/doctor/book-appointment")
public class BookAppointmentServlet extends HttpServlet {

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

        String idPazStr = req.getParameter("idPaziente");
        String tipo = req.getParameter("tipo");
        String dataStr = req.getParameter("data");
        String oraStr = req.getParameter("ora");

        if (idPazStr == null || tipo == null || dataStr == null || oraStr == null
                || idPazStr.isBlank() || tipo.isBlank() || dataStr.isBlank() || oraStr.isBlank()) {
            // input non validi → torno alla pagina precedente
            resp.sendRedirect(req.getHeader("Referer"));
            return;
        }

        try {
            long idPaziente = Long.parseLong(idPazStr);

            LocalDate data = LocalDate.parse(dataStr);
            LocalTime ora = LocalTime.parse(oraStr);
            LocalDateTime ldt = LocalDateTime.of(data, ora);
            Timestamp ts = Timestamp.valueOf(ldt);

            Appuntamento a = new Appuntamento();
            a.setIdPaziente(idPaziente);
            a.setIdMedico(idMedico);
            a.setTipo(tipo);
            a.setDataOra(ts);

            AppuntamentoDAO.insert(a);

            // Ritorno alla chat con lo stesso paziente
            resp.sendRedirect(req.getContextPath() + "/doctor/chat?id=" + idPaziente);

        } catch (Exception e) {
            throw new ServletException("Errore durante la prenotazione dell'appuntamento", e);
        }
    }
}
