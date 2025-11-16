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

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
            return null;
        }
    }

    private static Paziente map(ResultSet rs) throws Exception {
        Paziente p = new Paziente();
        p.setIdPaz(rs.getLong("id_paz"));
        p.setIdUtente(rs.getLong("id_utente"));
        p.setIdMedico(rs.getLong("id_medico"));
        p.setNome(rs.getString("nome"));
        p.setCognome(rs.getString("cognome"));
        p.setDataN(rs.getDate("data_n").toLocalDate());
        p.setSesso(rs.getString("sesso"));
        p.setCf(rs.getString("cf"));
        p.setMail(rs.getString("mail"));
        p.setNTel(rs.getLong("n_tel"));
        return p;
    }
}
