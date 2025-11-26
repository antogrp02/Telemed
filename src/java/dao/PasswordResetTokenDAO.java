package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class PasswordResetTokenDAO {

    public static class PasswordResetToken {
        public long id;
        public long idUtente;
        public String token;
        public Timestamp expiresAt;
        public boolean used;
    }

    public static void createToken(long idUtente, String token, Timestamp expiresAt) throws Exception {
        String sql = "INSERT INTO password_reset_token (id_utente, token, expires_at, used) VALUES (?, ?, ?, FALSE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idUtente);
            ps.setString(2, token);
            ps.setTimestamp(3, expiresAt);
            ps.executeUpdate();
        }
    }

    /**
     * Restituisce il token se esiste, non è scaduto e non è già usato.
     */
    public static PasswordResetToken findValidByToken(String token) throws Exception {
        String sql = "SELECT id, id_utente, token, expires_at, used " +
                     "FROM password_reset_token " +
                     "WHERE token = ? AND used = FALSE AND expires_at > NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                PasswordResetToken t = new PasswordResetToken();
                t.id = rs.getLong("id");
                t.idUtente = rs.getLong("id_utente");
                t.token = rs.getString("token");
                t.expiresAt = rs.getTimestamp("expires_at");
                t.used = rs.getBoolean("used");
                return t;
            }
        }
    }

    public static void markUsed(long id) throws Exception {
        String sql = "UPDATE password_reset_token SET used = TRUE WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
