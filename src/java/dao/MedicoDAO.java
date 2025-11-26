/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Medico;

import java.sql.*;

public class MedicoDAO {

    public static Medico getByIdUtente(long idUtente) throws Exception {
        String sql = "SELECT * FROM medico WHERE id_utente = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idUtente);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
            return null;
        }
    }

    public static Medico getByIdMedico(long idMedico) throws Exception {
        String sql = "SELECT * FROM medico WHERE id_medico = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idMedico);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
            return null;
        }
    }

    private static Medico map(ResultSet rs) throws Exception {
        Medico m = new Medico();
        m.setIdMedico(rs.getLong("id_medico"));
        m.setIdUtente(rs.getLong("id_utente"));
        m.setNome(rs.getString("nome"));
        m.setCognome(rs.getString("cognome"));
        m.setCf(rs.getString("cf"));
        m.setMail(rs.getString("mail"));
        return m;
    }

    // tutti i medici (per tabella Admin)
    public static java.util.List<Medico> getAll() throws Exception {
        String sql = "SELECT * FROM medico ORDER BY cognome, nome";
        java.util.List<Medico> out = new java.util.ArrayList<>();

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(map(rs));
            }
        }
        return out;
    }

    // medico per codice fiscale
    public static Medico getByCf(String cf) throws Exception {
        String sql = "SELECT * FROM medico WHERE cf = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cf);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
            return null;
        }
    }

    // inserimento nuovo medico (senza account)
    public static long insert(Medico m) throws Exception {
        String sql = "INSERT INTO medico (nome, cognome, cf, mail, id_utente) "
                + "VALUES (?, ?, ?, ?, NULL)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getNome());
            ps.setString(2, m.getCognome());
            ps.setString(3, m.getCf());
            ps.setString(4, m.getMail());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getLong(1);
            } else {
                throw new Exception("Errore interno: impossibile generare ID medico.");
            }

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                String msg = e.getMessage();

                if (msg.contains("medico_cf_key")) {
                    throw new Exception("Errore: esiste già un medico con questo codice fiscale.");
                }

                if (msg.contains("medico_mail_key")) {
                    throw new Exception("Errore: esiste già un medico con questa email.");
                }
            }

            throw e;
        }
    }

    // aggiorna dati anagrafici medico
    public static void updateAnagrafica(Medico m) throws Exception {
        String sql = "UPDATE medico SET nome=?, cognome=?, cf=?, mail=? WHERE id_medico=?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getNome());
            ps.setString(2, m.getCognome());
            ps.setString(3, m.getCf());
            ps.setString(4, m.getMail());
            ps.setLong(5, m.getIdMedico());

            ps.executeUpdate();

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                String msg = e.getMessage();

                if (msg.contains("medico_cf_key")) {
                    throw new Exception("Errore: codice fiscale già associato a un altro medico.");
                }

                if (msg.contains("medico_mail_key")) {
                    throw new Exception("Errore: email già utilizzata da un altro medico.");
                }
            }

            throw e;
        }
    }

    // rimuove legame account dal medico
    public static void clearIdUtente(long idMedico) throws Exception {
        String sql = "UPDATE medico SET id_utente = NULL WHERE id_medico = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idMedico);
            ps.executeUpdate();
        }
    }

    // --- CHECK ESISTENZA DATI ---
    public static boolean existsCfMedico(String cf) throws Exception {
        String sql = "SELECT 1 FROM medico WHERE cf = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cf);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public static boolean existsMailMedico(String mail) throws Exception {
        String sql = "SELECT 1 FROM medico WHERE mail = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mail);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
    
        public static Medico getByMail(String mail) throws Exception {
        String sql = "SELECT * FROM medico WHERE mail = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mail);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Medico m = new Medico();
                m.setIdMedico(rs.getLong("id_medico"));
                m.setIdUtente(rs.getLong("id_utente"));
                m.setNome(rs.getString("nome"));
                m.setCognome(rs.getString("cognome"));
                m.setMail(rs.getString("mail"));
                // altri campi se servono
                return m;
            }
        }
    }


}
