package controllers;

import dao.CredenzialiDAO;
import dao.MedicoDAO;
import dao.PazienteDAO;
import dao.UtenteDAO;

import model.Medico;
import model.Paziente;
import model.Utente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    /** Forza UTF-8 correttamente */
    private void forceUtf(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.setCharacterEncoding("UTF-8");
        } catch (Exception ignored) {}
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
    }

    /** Sicurezza: solo admin */
    private boolean ensureAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        Object roleObj = session.getAttribute("role");
        if (!(roleObj instanceof Integer) || ((Integer) roleObj) != 2) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        forceUtf(req, resp);

        if (!ensureAdmin(req, resp)) return;

        try {
            List<Paziente> pazienti = PazienteDAO.getAll();
            List<Medico> medici = MedicoDAO.getAll();

            req.setAttribute("pazienti", pazienti);
            req.setAttribute("medici", medici);

            // messaggi
            String msg = req.getParameter("msg");
            String err = req.getParameter("err");
            if (msg != null && !msg.isEmpty()) req.setAttribute("message", msg);
            if (err != null && !err.isEmpty()) req.setAttribute("error", err);

            req.getRequestDispatcher("/admin_users.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        forceUtf(req, resp);

        if (!ensureAdmin(req, resp)) return;

        String op = req.getParameter("op");
        if (op == null) op = "";

        String ctx = req.getContextPath();

        try {

            switch (op) {

                case "addPerson":
                    handleAddPerson(req);
                    redirectMsg(resp, ctx, "Persona registrata correttamente.");
                    return;

                case "createAccount":
                    handleCreateAccount(req);
                    redirectMsg(resp, ctx, "Account creato correttamente.");
                    return;

                case "assignDoctor":
                    handleAssignDoctor(req);
                    redirectMsg(resp, ctx, "Paziente assegnato al medico.");
                    return;

                case "unassignDoctor":
                    handleUnassignDoctor(req);
                    redirectMsg(resp, ctx, "Paziente disassegnato dal medico.");
                    return;

                case "deleteAccount":
                    handleDeleteAccount(req);
                    redirectMsg(resp, ctx, "Account eliminato correttamente.");
                    return;

                case "editPerson":
                    handleEditPerson(req);
                    redirectMsg(resp, ctx, "Dati anagrafici aggiornati.");
                    return;

                default:
                    redirectErr(resp, ctx, "Operazione non riconosciuta.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectErr(resp, ctx, "Errore: " + e.getMessage());
        }
    }

    /** Redirect con UTF-8 reale */
    private void redirectMsg(HttpServletResponse resp, String ctx, String msg) throws IOException {
        resp.sendRedirect(ctx + "/admin/users?msg=" +
                URLEncoder.encode(msg, StandardCharsets.UTF_8).replace("+", "%20"));
    }

    private void redirectErr(HttpServletResponse resp, String ctx, String err) throws IOException {
        resp.sendRedirect(ctx + "/admin/users?err=" +
                URLEncoder.encode(err, StandardCharsets.UTF_8).replace("+", "%20"));
    }

    // ----------------------
    //     HANDLER ADMIN
    // ----------------------

    /** A) Aggiungi persona */
    private void handleAddPerson(HttpServletRequest req) throws Exception {
        String type = req.getParameter("type");

        if ("paziente".equals(type)) {

            String cf = req.getParameter("cf");
            String mail = req.getParameter("mail");
            String telStr = req.getParameter("tel");
            long tel = (telStr != null && !telStr.isBlank()) ? Long.parseLong(telStr) : 0L;

            // --- VALIDAZIONI INCROCIATE ---
            if (PazienteDAO.existsByCf(cf))
                throw new Exception("Esiste già un paziente con questo codice fiscale.");

            if (PazienteDAO.existsByMail(mail))
                throw new Exception("Email già utilizzata da un altro paziente.");

            if (PazienteDAO.existsByTel(tel))
                throw new Exception("Numero di telefono già utilizzato da un altro paziente.");

            if (MedicoDAO.existsMailMedico(mail))
                throw new Exception("Questa email è già utilizzata da un medico.");

            if (MedicoDAO.existsCfMedico(cf))
                throw new Exception("Esiste già un medico con questo codice fiscale.");

            // --- CREAZIONE ---
            Paziente p = new Paziente();
            p.setNome(req.getParameter("nome"));
            p.setCognome(req.getParameter("cognome"));
            p.setCf(cf);
            p.setMail(mail);
            p.setNTel(tel);
            p.setSesso(req.getParameter("sesso"));
            p.setDataN(Date.valueOf(req.getParameter("data_n")));
            p.setIdMedico(0);

            PazienteDAO.insert(p);
        }

        if ("medico".equals(type)) {

            String cf = req.getParameter("cf");
            String mail = req.getParameter("mail");

            if (MedicoDAO.existsCfMedico(cf))
                throw new Exception("Esiste già un medico con questo codice fiscale.");

            if (MedicoDAO.existsMailMedico(mail))
                throw new Exception("Email già utilizzata da un altro medico.");

            if (PazienteDAO.existsByMail(mail))
                throw new Exception("Questa email è già utilizzata da un paziente.");

            if (PazienteDAO.existsByCf(cf))
                throw new Exception("Esiste già un paziente con questo codice fiscale.");

            Medico m = new Medico();
            m.setNome(req.getParameter("nome"));
            m.setCognome(req.getParameter("cognome"));
            m.setCf(cf);
            m.setMail(mail);

            MedicoDAO.insert(m);
        }
    }

    /** B) Creazione account */
    private void handleCreateAccount(HttpServletRequest req) throws Exception {
        String type = req.getParameter("type");
        String cf = req.getParameter("cf");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (UtenteDAO.findByUsername(username) != null)
            throw new Exception("Username già esistente.");

        if ("paziente".equals(type)) {

            Paziente p = PazienteDAO.getByCf(cf);
            if (p == null) throw new Exception("Paziente non presente nel database.");
            if (p.getIdUtente() != 0) throw new Exception("Questo paziente ha già un account.");

            Utente u = UtenteDAO.insert(username);
            CredenzialiDAO.insert(username, password, 0);

            try (var conn = dao.DBConnection.getConnection();
                 var ps = conn.prepareStatement(
                         "UPDATE paziente SET id_utente=? WHERE id_paz=?")) {
                ps.setLong(1, u.getIdUtente());
                ps.setLong(2, p.getIdPaz());
                ps.executeUpdate();
            }
        }

        if ("medico".equals(type)) {

            Medico m = MedicoDAO.getByCf(cf);
            if (m == null) throw new Exception("Medico non presente nel database.");
            if (m.getIdUtente() != 0) throw new Exception("Questo medico ha già un account.");

            Utente u = UtenteDAO.insert(username);
            CredenzialiDAO.insert(username, password, 1);

            try (var conn = dao.DBConnection.getConnection();
                 var ps = conn.prepareStatement(
                         "UPDATE medico SET id_utente=? WHERE id_medico=?")) {
                ps.setLong(1, u.getIdUtente());
                ps.setLong(2, m.getIdMedico());
                ps.executeUpdate();
            }
        }
    }

    /** C) Assegna paziente */
    private void handleAssignDoctor(HttpServletRequest req) throws Exception {
        String cfPaz = req.getParameter("cf_paz");
        String cfMed = req.getParameter("cf_med");

        Paziente p = PazienteDAO.getByCf(cfPaz);
        if (p == null) throw new Exception("Paziente non trovato.");

        if (p.getIdMedico() != 0)
            throw new Exception("Paziente già assegnato a un medico.");

        Medico m = MedicoDAO.getByCf(cfMed);
        if (m == null) throw new Exception("Medico non trovato.");

        PazienteDAO.assignToMedico(p.getIdPaz(), m.getIdMedico());
    }

    /** D) Disassegna */
    private void handleUnassignDoctor(HttpServletRequest req) throws Exception {
        String cfPaz = req.getParameter("cf_paz");

        Paziente p = PazienteDAO.getByCf(cfPaz);
        if (p == null) throw new Exception("Paziente non trovato.");

        if (p.getIdMedico() == 0)
            throw new Exception("Paziente non ha un medico assegnato.");

        PazienteDAO.removeMedico(p.getIdPaz());
    }

    /** E) Eliminazione account */
    private void handleDeleteAccount(HttpServletRequest req) throws Exception {
        String type = req.getParameter("type");
        String cf = req.getParameter("cf");

        if ("paziente".equals(type)) {
            Paziente p = PazienteDAO.getByCf(cf);
            if (p == null) throw new Exception("Paziente non trovato.");
            if (p.getIdUtente() == 0) throw new Exception("Il paziente non ha un account.");

            Utente u = UtenteDAO.findById(p.getIdUtente());
            if (u != null) {
                CredenzialiDAO.deleteByUsername(u.getUsername());
                UtenteDAO.delete(u.getIdUtente());
            }
            PazienteDAO.clearIdUtente(p.getIdPaz());
        }

        if ("medico".equals(type)) {
            Medico m = MedicoDAO.getByCf(cf);
            if (m == null) throw new Exception("Medico non trovato.");
            if (m.getIdUtente() == 0) throw new Exception("Il medico non ha un account.");

            Utente u = UtenteDAO.findById(m.getIdUtente());
            if (u != null) {
                CredenzialiDAO.deleteByUsername(u.getUsername());
                UtenteDAO.delete(u.getIdUtente());
            }
            MedicoDAO.clearIdUtente(m.getIdMedico());
        }
    }

    /** F) Modifica */
    private void handleEditPerson(HttpServletRequest req) throws Exception {
        String type = req.getParameter("type");
        String cf = req.getParameter("cf");

        if ("paziente".equals(type)) {
            Paziente p = PazienteDAO.getByCf(cf);
            if (p == null) throw new Exception("Paziente non trovato.");

            p.setNome(req.getParameter("nome"));
            p.setCognome(req.getParameter("cognome"));
            p.setMail(req.getParameter("mail"));
            String telStr = req.getParameter("tel");
            long tel = (telStr != null && !telStr.isBlank()) ? Long.parseLong(telStr) : 0L;
            p.setNTel(tel);
            p.setSesso(req.getParameter("sesso"));
            p.setDataN(Date.valueOf(req.getParameter("data_n")));

            String newCf = req.getParameter("new_cf");
            if (newCf != null && !newCf.isBlank()) p.setCf(newCf);

            PazienteDAO.updateAnagrafica(p);
        }

        if ("medico".equals(type)) {
            Medico m = MedicoDAO.getByCf(cf);
            if (m == null) throw new Exception("Medico non trovato.");

            m.setNome(req.getParameter("nome"));
            m.setCognome(req.getParameter("cognome"));
            m.setMail(req.getParameter("mail"));

            String newCf = req.getParameter("new_cf");
            if (newCf != null && !newCf.isBlank()) m.setCf(newCf);

            MedicoDAO.updateAnagrafica(m);
        }
    }
}
