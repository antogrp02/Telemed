/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Alert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    private static Alert map(ResultSet rs) throws Exception {
        Alert a = new Alert();
        a.setIdAlert(rs.getLong("id_alert"));
        a.setIdPaz(rs.getLong("id_paz"));
        a.setRiskData(rs.getTimestamp("risk_data"));
        a.setIdMedico(rs.getLong("id_medico"));
        a.setMessaggio(rs.getString("messaggio"));
        a.setVisto(rs.getBoolean("visto"));
        a.setArchiviato(rs.getBoolean("archiviato"));
        return a;
    }

    public static List<Alert> getAlertsByPaziente(long idPaz) {
        List<Alert> list = new ArrayList<>();

        String sql = "SELECT * FROM alert WHERE id_paz = ? AND archiviato = false ORDER BY risk_data DESC";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public static void insert(Alert a) throws Exception {
        String sql = "INSERT INTO alert (id_paz, risk_data, id_medico, messaggio) VALUES (?,?,?,?)";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, a.getIdPaz());
            ps.setTimestamp(2, a.getRiskData());
            ps.setLong(3, a.getIdMedico());
            ps.setString(4, a.getMessaggio());

            ps.executeUpdate();
        }
    }

    public static List<Alert> getAlertsByMedico(long idMedico) {
        List<Alert> list = new ArrayList<>();

        String sql = "SELECT * FROM alert WHERE id_medico = ? AND archiviato = false ORDER BY risk_data DESC";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idMedico);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

}
