package controllers;

import dao.PazienteDAO;
import dao.MedicoDAO;
import dao.UtenteDAO;
import dao.PasswordResetTokenDAO;

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/forgot_password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        System.out.println("\n=== [ForgotPasswordServlet] POST INIZIO ===\n");

        try {
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");

            String email = req.getParameter("email");

            System.out.println("📧 Email ricevuta: " + email);

            if (email == null || email.trim().isEmpty()) {
                req.setAttribute("error", "Inserisci un indirizzo email.");
                req.getRequestDispatcher("/forgot_password.jsp").forward(req, resp);
                return;
            }

            email = email.trim();

            // ======================================================
            // 1️⃣ CERCA PAZIENTE
            // ======================================================
            Paziente p = null;
            try {
                System.out.println("🔎 Ricerca paziente...");
                p = PazienteDAO.getByMail(email);
                System.out.println("🔍 Paziente trovato: " + (p != null));
            } catch (Exception e) {
                System.out.println("⚠ Errore in PazienteDAO.getByMail");
                e.printStackTrace();
            }

            // Se è un paziente anagrafico-only → NON può resettare password
            if (p != null && p.getIdUtente() == 0) {
                System.out.println("⚠ Paziente anagrafico senza account. Niente reset.");
                p = null;
            }

            // ======================================================
            // 2️⃣ CERCA MEDICO (solo se non è un paziente registrato)
            // ======================================================
            Medico m = null;
            if (p == null) {
                try {
                    System.out.println("🔎 Ricerca medico...");
                    m = MedicoDAO.getByMail(email);
                    System.out.println("🔍 Medico trovato: " + (m != null));
                } catch (Exception e) {
                    System.out.println("⚠ Errore in MedicoDAO.getByMail");
                    e.printStackTrace();
                }
            }

            // Anche il medico deve avere id_utente valido
            if (m != null && m.getIdUtente() == 0) {
                System.out.println("⚠ Medico anagrafico senza account. Niente reset.");
                m = null;
            }

            // ======================================================
            // 3️⃣ SE NEANCHE PAZIENTE NE’ MEDICO ESISTE
            //     -> messaggio neutro
            // ======================================================
            if (p == null && m == null) {
                System.out.println("❌ Nessun utente registrato trovato con questa email");
                req.setAttribute("success", "Se l'indirizzo è presente nei nostri sistemi, riceverai una email.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            // ======================================================
            // 4️⃣ UTENTE VALIDO TROVATO
            // ======================================================
            long idUtente = (p != null) ? p.getIdUtente() : m.getIdUtente();
            System.out.println("🆔 ID Utente valido: " + idUtente);

            // ======================================================
            // 5️⃣ GENERO TOKEN
            // ======================================================
            String token = UUID.randomUUID().toString().replace("-", "");
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 3600_000);

            System.out.println("🔐 Token: " + token);
            System.out.println("⏰ Scadenza: " + expiresAt);

            PasswordResetTokenDAO.createToken(idUtente, token, expiresAt);
            System.out.println("💾 Token salvato nel DB");

            // ======================================================
            // 6️⃣ COSTRUISCO LINK RESET
            // ======================================================
            String scheme = req.getScheme();
            String serverName = req.getServerName();
            int serverPort = req.getServerPort();
            String contextPath = req.getContextPath();

            String baseUrl = scheme + "://" + serverName
                    + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort)
                    + contextPath;

            String resetLink = baseUrl + "/password/reset?token="
                    + URLEncoder.encode(token, StandardCharsets.UTF_8);

            System.out.println("🔗 Reset link: " + resetLink);

            // ======================================================
            // 7️⃣ INVIO EMAIL
            // ======================================================
            System.out.println("📨 Invio email a " + email + "...");

            try {
                MailUtil.sendPasswordResetMail(email, resetLink);
                System.out.println("✅ Email inviata!");
            } catch (Exception e) {
                System.out.println("❌ ERRORE INVIO EMAIL");
                e.printStackTrace();
            }

            // ======================================================
            // 8️⃣ RISPOSTA NEUTRA PER L’UTENTE
            // ======================================================
            req.setAttribute("success",
                    "Se l'indirizzo è presente nei nostri sistemi, riceverai una email.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);

        } catch (Exception e) {
            System.out.println("❌ ERRORE GRAVE:");
            e.printStackTrace();
            throw new ServletException(e);
        }

        System.out.println("\n=== [ForgotPasswordServlet] POST FINE ===\n");
    }
}
