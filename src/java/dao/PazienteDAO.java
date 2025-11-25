/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Paziente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PazienteDAO {

    // paziente associato a uno specifico utente
    public static Paziente getByIdUtente(long idUtente) throws Exception {
        String sql = "SELECT * FROM paziente WHERE id_utente = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idUtente);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
            return null;
        }
    }

    // tutti i pazienti in carico a un dato medico
    public static List<Paziente> getByIdMedico(long idMedico) throws Exception {
        String sql = "SELECT * FROM paziente WHERE id_medico = ?";
        List<Paziente> out = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idMedico);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                out.add(map(rs));
            }
        }
        return out;
    }

    // singolo paziente per id_paz
    public static Paziente getByIdPaziente(long idPaz) throws Exception {
        String sql = "SELECT * FROM paziente WHERE id_paz = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
            return null;
        }
    }

    public static List<Paziente> searchByMedico(long idMedico, String q) {
        List<Paziente> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            String sql
                    = "SELECT id_paz, nome, cognome, cf "
                    + // JSON leggero
                    "FROM paziente "
                    + "WHERE id_medico=? AND ("
                    + "LOWER(nome) LIKE LOWER(?) OR "
                    + "LOWER(cognome) LIKE LOWER(?) OR "
                    + "LOWER(cf) LIKE LOWER(?)"
                    + ") ORDER BY cognome LIMIT 10";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, idMedico);
            ps.setString(2, "%" + q + "%");
            ps.setString(3, "%" + q + "%");
            ps.setString(4, "%" + q + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Paziente p = new Paziente();
                p.setIdPaz(rs.getLong("id_paz"));
                p.setNome(rs.getString("nome"));
                p.setCognome(rs.getString("cognome"));
                p.setCf(rs.getString("cf"));
                lista.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    private static Paziente map(ResultSet rs) throws Exception {
        Paziente p = new Paziente();
        p.setIdPaz(rs.getLong("id_paz"));
        p.setIdUtente(rs.getLong("id_utente"));
        p.setIdMedico(rs.getLong("id_medico"));
        p.setNome(rs.getString("nome"));
        p.setCognome(rs.getString("cognome"));
        p.setDataN(rs.getDate("data_n"));
        p.setSesso(rs.getString("sesso"));
        p.setCf(rs.getString("cf"));
        p.setMail(rs.getString("mail"));
        p.setNTel(rs.getLong("n_tel"));
        return p;
    }

    // tutti i pazienti (per tabella Admin)
    public static List<Paziente> getAll() throws Exception {
        String sql = "SELECT * FROM paziente ORDER BY cognome, nome";
        List<Paziente> out = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(map(rs));
            }
        }
        return out;
    }

    // paziente per codice fiscale
    public static Paziente getByCf(String cf) throws Exception {
        String sql = "SELECT * FROM paziente WHERE cf = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cf);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
            return null;
        }
    }

    // inserimento nuovo paziente (senza medico e senza account)
    public static long insert(Paziente p) throws Exception {
        String sql = "INSERT INTO paziente "
                + "(nome, cognome, data_n, sesso, cf, id_medico, mail, n_tel, id_utente) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getCognome());
            ps.setDate(3, p.getDataN());
            ps.setString(4, p.getSesso());
            ps.setString(5, p.getCf());
            if (p.getIdMedico() > 0) {
                ps.setLong(6, p.getIdMedico());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.setString(7, p.getMail());
            ps.setLong(8, p.getNTel());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getLong(1);
            } else {
                throw new Exception("Errore interno: impossibile generare ID paziente.");
            }

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                String msg = e.getMessage();

                if (msg.contains("paziente_cf_key")) {
                    throw new Exception("Esiste già un paziente con questo codice fiscale.");
                }

                if (msg.contains("paziente_mail_key")) {
                    throw new Exception("Esiste già un paziente con questa email.");
                }

                if (msg.contains("paziente_n_tel_key")) {
                    throw new Exception("Esiste già un paziente con questo numero di telefono.");
                }
            }

            throw e;
        }
    }

    // aggiorna dati anagrafici paziente (no account)
    public static void updateAnagrafica(Paziente p) throws Exception {
        String sql = "UPDATE paziente SET nome=?, cognome=?, data_n=?, sesso=?, cf=?, mail=?, n_tel=? "
                + "WHERE id_paz=?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getCognome());
            ps.setDate(3, p.getDataN());
            ps.setString(4, p.getSesso());
            ps.setString(5, p.getCf());
            ps.setString(6, p.getMail());
            ps.setLong(7, p.getNTel());
            ps.setLong(8, p.getIdPaz());

            ps.executeUpdate();

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                String msg = e.getMessage();

                if (msg.contains("paziente_cf_key")) {
                    throw new Exception("Codice fiscale già utilizzato da un altro paziente.");
                }

                if (msg.contains("paziente_mail_key")) {
                    throw new Exception("Email già utilizzata da un altro paziente.");
                }

                if (msg.contains("paziente_n_tel_key")) {
                    throw new Exception("Numero di telefono già utilizzato da un altro paziente.");
                }
            }

            throw e;
        }
    }

    // assegna paziente a medico
    public static void assignToMedico(long idPaz, long idMedico) throws Exception {
        String sql = "UPDATE paziente SET id_medico = ? WHERE id_paz = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idMedico);
            ps.setLong(2, idPaz);
            ps.executeUpdate();
        }
    }

    // disassegna paziente dal medico
    public static void removeMedico(long idPaz) throws Exception {
        String sql = "UPDATE paziente SET id_medico = NULL WHERE id_paz = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ps.executeUpdate();
        }
    }

    // rimuove legame account dal paziente
    public static void clearIdUtente(long idPaz) throws Exception {
        String sql = "UPDATE paziente SET id_utente = NULL WHERE id_paz = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ps.executeUpdate();
        }
    }

    // --- CHECK ESISTENZA DATI ---
    public static boolean existsByCf(String cf) throws Exception {
        String sql = "SELECT 1 FROM paziente WHERE cf = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cf);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public static boolean existsByMail(String mail) throws Exception {
        String sql = "SELECT 1 FROM paziente WHERE mail = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mail);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public static boolean existsByTel(long tel) throws Exception {
        String sql = "SELECT 1 FROM paziente WHERE n_tel = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tel);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

}
