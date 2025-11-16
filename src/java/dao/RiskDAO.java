/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Risk;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RiskDAO {

    public static void insert(Risk r) throws SQLException {
        String sql = "INSERT INTO risk (id_paz, data, risk_score) VALUES (?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, r.getIdPaz());
            ps.setTimestamp(2, r.getData());
            ps.setFloat(3, r.getRiskScore());
            ps.executeUpdate();
        }
    }

    public static Risk getLastByPatient(long idPaz) throws SQLException {
        String sql = "SELECT data, risk_score FROM risk WHERE id_paz = ? ORDER BY data DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Risk r = new Risk();
                r.setIdPaz(idPaz);
                r.setData(rs.getTimestamp("data"));
                r.setRiskScore(rs.getFloat("risk_score"));
                return r;
            }
        }
        return null;
    }

    /** 🔥 NUOVO: rischio ultimi 7 giorni */
    public static List<Risk> getLast7Days(long idPaz) throws SQLException {

        String sql = """
            SELECT data, risk_score
            FROM risk
            WHERE id_paz = ?
            AND data >= NOW() - INTERVAL '7 days'
            ORDER BY data ASC
        """;

        List<Risk> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Risk r = new Risk();
                r.setIdPaz(idPaz);
                r.setData(rs.getTimestamp("data"));
                r.setRiskScore(rs.getFloat("risk_score"));
                list.add(r);
            }
        }
        return list;
    }
}

