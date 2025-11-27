/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import dao.CredenzialiDAO;
import dao.CredenzialiDAO.Credenziale;
import dao.UtenteDAO;
import dao.PazienteDAO;
import dao.MedicoDAO;

import model.Utente;
import model.Paziente;
import model.Medico;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            // 1) Verifica credenziali
            Credenziale c = CredenzialiDAO.checkLogin(username, password);
            if (c == null) {
                req.setAttribute("error", "Credenziali non valide");
                req.getRequestDispatcher("login.jsp").forward(req, resp);
                return;
            }

            // PASSWORD TEMPORANEA → FORZARE CAMBIO
            if (c.ForcedChange) {
                HttpSession session = req.getSession(true);
                session.setAttribute("force_username", c.username);
                session.setAttribute("force_role", c.role);  // ci servirà per il redirect finale

                resp.sendRedirect(req.getContextPath() + "<%= request.getContextPath() %>/change_password.jsp");
                return;
            }

            // 2) Trova utente (id_utente)
            Utente u = UtenteDAO.findByUsername(username);
            if (u == null) {
                req.setAttribute("error", "Errore: utente non trovato.");
                req.getRequestDispatcher("login.jsp").forward(req, resp);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("id_utente", u.getIdUtente());
            session.setAttribute("role", c.role);

            // 3) Routing in base al ruolo
            if (c.role == 0) {  // PAZIENTE
                Paziente p = PazienteDAO.getByIdUtente(u.getIdUtente());
                if (p == null) {
                    req.setAttribute("error", "Nessun profilo paziente collegato all'account.");
                    req.getRequestDispatcher("login.jsp").forward(req, resp);
                    return;
                }
                session.setAttribute("id_paziente", p.getIdPaz());
                session.setAttribute("id_medico", p.getIdMedico());
                resp.sendRedirect("patient/dashboard");
                return;
            }

            if (c.role == 1) {  // MEDICO
                Medico m = MedicoDAO.getByIdUtente(u.getIdUtente());
                if (m == null) {
                    req.setAttribute("error", "Nessun profilo medico collegato all'account.");
                    req.getRequestDispatcher("login.jsp").forward(req, resp);
                    return;
                }
                session.setAttribute("id_medico", m.getIdMedico());
                resp.sendRedirect("doctor/dashboard");
                return;
            }

            // ADMIN (role == 2)
            resp.sendRedirect("admin/users");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
