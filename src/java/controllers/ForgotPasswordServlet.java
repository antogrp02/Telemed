package controllers;

import dao.PazienteDAO;
import dao.MedicoDAO;
import dao.UtenteDAO;
import dao.PasswordResetTokenDAO;
import dao.PasswordResetTokenDAO.PasswordResetToken;

import model.Paziente;
import model.Medico;
import model.Utente;

import utils.MailUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.UUID;

@WebServlet("/password/forgot")
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/forgot_password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            String email = req.getParameter("email");
            if (email == null || email.trim().isEmpty()) {
                req.setAttribute("error", "Inserisci un indirizzo email.");
                req.getRequestDispatcher("/forgot_password.jsp").forward(req, resp);
                return;
            }

            email = email.trim();

            Paziente p = null;
            Medico m = null;

            try {
                p = PazienteDAO.getByMail(email);
            } catch (Exception ignored) {}

            if (p == null) {
                try {
                    m = MedicoDAO.getByMail(email);
                } catch (Exception ignored) {}
            }

            if (p == null && m == null) {
                // Messaggio neutro (per non rivelare se l'email esiste)
                req.setAttribute("success", "Se l'indirizzo è presente nei nostri sistemi, riceverai a breve una email con le istruzioni.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            long idUtente = (p != null) ? p.getIdUtente() : m.getIdUtente();

            // Genera token
            String token = UUID.randomUUID().toString().replace("-", "");
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 3600_000L); // 1 ora

            PasswordResetTokenDAO.createToken(idUtente, token, expiresAt);

            // Costruisci URL assoluto
            String scheme = req.getScheme();              // http o https
            String serverName = req.getServerName();
            int serverPort = req.getServerPort();
            String contextPath = req.getContextPath();

            String baseUrl = scheme + "://" + serverName +
                    ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort) +
                    contextPath;

            String resetLink = baseUrl + "/password/reset?token=" +
                    URLEncoder.encode(token, StandardCharsets.UTF_8);

            // Invia email
            try {
                MailUtil.sendPasswordResetMail(email, resetLink);
            } catch (Exception e) {
                // Se la mail fallisce, non rivelo dettagli all'utente
                e.printStackTrace();
            }

            req.setAttribute("success", "Se l'indirizzo è presente nei nostri sistemi, riceverai a breve una email con le istruzioni.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
