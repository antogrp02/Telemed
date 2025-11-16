/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Parametri;
import java.sql.*;

public class ParametriDAO {

    public static Parametri getLastByPatient(long idPaz) throws SQLException {
        String sql = "SELECT * FROM parametri WHERE id_paz = ? ORDER BY data DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPaz);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public static void insert(Parametri p) throws SQLException {
        String sql =
            "INSERT INTO parametri (" +
            "id_paz, data, " +
            "hr_curr, rhr_curr, hrv_rmssd_curr, spo2_curr, resp_rate_curr, bioimp_curr, weight_curr, steps_curr, " +
            "hr_7d, rhr_7d, hrv_rmssd_7d, spo2_7d, resp_rate_7d, biomip_7d, weight_7d, steps_7d, " +
            "hr_bs, rhr_bs, hrv_rmssd_bs, spo2_bs, resp_rate_bs, bioimp_bs, weight_bs, steps_bs" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1,  p.getIdPaz());
            ps.setTimestamp(2, p.getData());
            ps.setDouble(3,  p.getHrCurr());
            ps.setDouble(4,  p.getRhrCurr());
            ps.setDouble(5,  p.getHrvRmssdCurr());
            ps.setDouble(6,  p.getSpo2Curr());
            ps.setDouble(7,  p.getRespRateCurr());
            ps.setDouble(8,  p.getBioimpCurr());
            ps.setDouble(9,  p.getWeightCurr());
            ps.setDouble(10, p.getStepsCurr());

            ps.setDouble(11, p.getHr7d());
            ps.setDouble(12, p.getRhr7d());
            ps.setDouble(13, p.getHrvRmssd7d());
            ps.setDouble(14, p.getSpo27d());
            ps.setDouble(15, p.getRespRate7d());
            ps.setDouble(16, p.getBioimp7d());
            ps.setDouble(17, p.getWeight7d());
            ps.setDouble(18, p.getSteps7d());

            ps.setDouble(19, p.getHrBs());
            ps.setDouble(20, p.getRhrBs());
            ps.setDouble(21, p.getHrvRmssdBs());
            ps.setDouble(22, p.getSpo2Bs());
            ps.setDouble(23, p.getRespRateBs());
            ps.setDouble(24, p.getBioimpBs());
            ps.setDouble(25, p.getWeightBs());
            ps.setDouble(26, p.getStepsBs());

            ps.executeUpdate();
        }
    }

    private static Parametri map(ResultSet rs) throws SQLException {
        Parametri p = new Parametri();
        p.setIdPaz(rs.getLong("id_paz"));
        p.setData(rs.getTimestamp("data"));

        p.setHrCurr(rs.getDouble("hr_curr"));
        p.setRhrCurr(rs.getDouble("rhr_curr"));
        p.setHrvRmssdCurr(rs.getDouble("hrv_rmssd_curr"));
        p.setSpo2Curr(rs.getDouble("spo2_curr"));
        p.setRespRateCurr(rs.getDouble("resp_rate_curr"));
        p.setBioimpCurr(rs.getDouble("bioimp_curr"));
        p.setWeightCurr(rs.getDouble("weight_curr"));
        p.setStepsCurr(rs.getDouble("steps_curr"));

        p.setHr7d(rs.getDouble("hr_7d"));
        p.setRhr7d(rs.getDouble("rhr_7d"));
        p.setHrvRmssd7d(rs.getDouble("hrv_rmssd_7d"));
        p.setSpo27d(rs.getDouble("spo2_7d"));
        p.setRespRate7d(rs.getDouble("resp_rate_7d"));
        p.setBioimp7d(rs.getDouble("biomip_7d"));
        p.setWeight7d(rs.getDouble("weight_7d"));
        p.setSteps7d(rs.getDouble("steps_7d"));

        p.setHrBs(rs.getDouble("hr_bs"));
        p.setRhrBs(rs.getDouble("rhr_bs"));
        p.setHrvRmssdBs(rs.getDouble("hrv_rmssd_bs"));
        p.setSpo2Bs(rs.getDouble("spo2_bs"));
        p.setRespRateBs(rs.getDouble("resp_rate_bs"));
        p.setBioimpBs(rs.getDouble("bioimp_bs"));
        p.setWeightBs(rs.getDouble("weight_bs"));
        p.setStepsBs(rs.getDouble("steps_bs"));

        return p;
    }
}
