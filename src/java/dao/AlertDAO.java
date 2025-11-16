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

    public static void insert(Alert a) throws SQLException {
        String sql =
            "INSERT INTO alert (id_paz, data, risk_score, soglia, stato) VALUES (?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, a.getIdPaz());
            ps.setTimestamp(2, a.getData());
            ps.setFloat(3, a.getRiskScore());
            ps.setFloat(4, a.getSoglia());
            ps.setString(5, a.getStato());
            ps.executeUpdate();
        }
    }

    public static List<Alert> listActive() throws SQLException {
        List<Alert> list = new ArrayList<>();
        String sql =
            "SELECT a.id_alert, a.id_paz, a.data, a.risk_score, a.stato, p.nome, p.cognome " +
            "FROM alert a " +
            "JOIN paziente p ON a.id_paz = p.id_paz " +
            "WHERE a.stato = 'attivo' " +
            "ORDER BY a.data DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Alert a = new Alert();
                a.setIdAlert(rs.getLong("id_alert"));
                a.setIdPaz(rs.getLong("id_paz"));
                a.setData(rs.getTimestamp("data"));
                a.setRiskScore(rs.getFloat("risk_score"));
                a.setStato(rs.getString("stato"));
                a.setNomePaz(rs.getString("nome"));
                a.setCognomePaz(rs.getString("cognome"));
                list.add(a);
            }
        }
        return list;
    }
}
