/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.AlertDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/doctor/alerts/archive")
public class DoctorAlertArchiveServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 1) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id alert mancante");
            return;
        }

        try {
            long idAlert = Long.parseLong(idStr);
            long idMedico = (long) s.getAttribute("id_medico");

            AlertDAO.archive(idAlert, idMedico);

            resp.sendRedirect(req.getContextPath() + "/doctor/alerts");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
