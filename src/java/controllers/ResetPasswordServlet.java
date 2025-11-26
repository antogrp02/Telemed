package controllers;

import dao.PasswordResetTokenDAO;
import dao.PasswordResetTokenDAO.PasswordResetToken;
import dao.UtenteDAO;
import dao.CredenzialiDAO;

import model.Utente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/password/reset")
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = req.getParameter("token");
        if (token == null || token.trim().isEmpty()) {
            req.setAttribute("error", "Link di reset non valido.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            PasswordResetToken t = PasswordResetTokenDAO.findValidByToken(token);
            if (t == null) {
                req.setAttribute("error", "Il link di reset non è valido o è scaduto.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("token", token);
            req.getRequestDispatcher("/reset_password.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            String token = req.getParameter("token");
            String password = req.getParameter("password");
            String confirm = req.getParameter("confirm");

            if (token == null || token.trim().isEmpty()) {
                req.setAttribute("error", "Token mancante.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            if (password == null || password.isEmpty() ||
                confirm == null || confirm.isEmpty()) {
                req.setAttribute("error", "Compila tutti i campi.");
                req.setAttribute("token", token);
                req.getRequestDispatcher("/reset_password.jsp").forward(req, resp);
                return;
            }

            if (!password.equals(confirm)) {
                req.setAttribute("error", "Le password non coincidono.");
                req.setAttribute("token", token);
                req.getRequestDispatcher("/reset_password.jsp").forward(req, resp);
                return;
            }

            if (password.length() < 6) {
                req.setAttribute("error", "La password deve avere almeno 6 caratteri.");
                req.setAttribute("token", token);
                req.getRequestDispatcher("/reset_password.jsp").forward(req, resp);
                return;
            }

            PasswordResetToken t = PasswordResetTokenDAO.findValidByToken(token);
            if (t == null) {
                req.setAttribute("error", "Il link di reset non è valido o è scaduto.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            Utente u = UtenteDAO.findById(t.idUtente);
            if (u == null) {
                req.setAttribute("error", "Utente non trovato.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            CredenzialiDAO.updatePassword(u.getUsername(), password);
            PasswordResetTokenDAO.markUsed(t.id);

            req.setAttribute("success", "Password aggiornata con successo. Ora puoi effettuare il login.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
