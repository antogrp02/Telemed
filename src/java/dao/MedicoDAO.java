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

    public static Medico getByIdMedico(long idMedico) throws Exception {
        String sql = "SELECT * FROM medico WHERE id_medico = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
        return m;
    }
}
