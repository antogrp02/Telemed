package controllers;

import dao.CredenzialiDAO;
import dao.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("force_username") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String username = (String) session.getAttribute("force_username");
        int role = (int) session.getAttribute("force_role");

        String newPass = req.getParameter("newpass");
        String conf = req.getParameter("conf");

        if (newPass == null || newPass.isBlank()) {
            req.setAttribute("err", "La password non può essere vuota.");
            req.getRequestDispatcher("/WEB-INF/change_password.jsp").forward(req, resp);
            return;
        }
        // 1) Controllo password coincidono
        if (!newPass.equals(conf)) {
            req.setAttribute("err", "Le password non coincidono.");
            req.getRequestDispatcher("/WEB-INF/change_password.jsp").forward(req, resp);
            return;
        }

        // 2) Recupera la password attuale dal DB
        String oldPassword = null;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT \"Password\" FROM credenziali WHERE \"Username\" = ?")) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                oldPassword = rs.getString("Password");
            }

        } catch (Exception e) {
            req.setAttribute("err", "Errore nel recupero della password attuale.");
            req.getRequestDispatcher("/WEB-INF/change_password.jsp").forward(req, resp);
            return;
        }

        if (oldPassword == null) {
            req.setAttribute("err", "Utente non trovato.");
            req.getRequestDispatcher("/WEB-INF/change_password.jsp").forward(req, resp);
            return;
        }

        // 3) Controllo: nuova password NON deve essere uguale alla vecchia
        if (newPass.equals(oldPassword)) {
            req.setAttribute("err", "La nuova password deve essere diversa da quella attuale.");
            req.getRequestDispatcher("/WEB-INF/change_password.jsp").forward(req, resp);
            return;
        }

        // 4) Aggiorna password e disattiva ForcedChange
        try {
            CredenzialiDAO.updatePasswordAndDisableFlag(username, newPass);
        } catch (Exception e) {
            req.setAttribute("err", "Errore durante l'aggiornamento della password.");
            req.getRequestDispatcher("/WEB-INF/change_password.jsp").forward(req, resp);
            return;
        }

        // 5) Pulizia sessione
        session.removeAttribute("force_username");
        session.removeAttribute("force_role");

        // 6) Redirect corretto in base al ruolo
        if (role == 0) {
            resp.sendRedirect(req.getContextPath() + "/patient/dashboard");
        } else if (role == 1) {
            resp.sendRedirect(req.getContextPath() + "/doctor/dashboard");
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        }
    }
}
