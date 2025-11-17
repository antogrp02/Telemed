/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Questionario;
import java.time.LocalDate;
import java.sql.*;

public class QuestionariDAO {

    private static final String INSERT_SQL
            = "INSERT INTO questionari (id_paziente, data, dispnea, edema, fatica, ortopnea, adl, vertigini) "
            + "VALUES (?,?,?,?,?,?,?,?)";

    public static void insert(Questionario q) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setLong(1, q.getIdPaziente());
            ps.setDate(2, q.getData());
            ps.setShort(3, q.getDispnea());
            ps.setShort(4, q.getEdema());
            ps.setShort(5, q.getFatica());
            ps.setShort(6, q.getOrtopnea());
            ps.setShort(7, q.getAdl());
            ps.setShort(8, q.getVertigini());
            ps.executeUpdate();
        }
    }

    public static boolean existsForDay(long idPaz, LocalDate date) throws SQLException {
        String sql = "SELECT 1 FROM questionari WHERE id_paziente=? AND data=? LIMIT 1";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ps.setDate(2, Date.valueOf(date));

            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public static Questionario getLastByPaziente(long idPaz) throws SQLException {

        String sql
                = "SELECT id_paziente, data, dispnea, edema, fatica, ortopnea, adl, vertigini "
                + "FROM questionari "
                + "WHERE id_paziente = ? "
                + "ORDER BY data DESC "
                + "LIMIT 1";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idPaz);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Questionario q = new Questionario();
                q.setIdPaziente(rs.getLong("id_paziente"));
                q.setData(rs.getDate("data"));
                q.setDispnea(rs.getShort("dispnea"));
                q.setEdema(rs.getShort("edema"));
                q.setFatica(rs.getShort("fatica"));
                q.setOrtopnea(rs.getShort("ortopnea"));
                q.setAdl(rs.getShort("adl"));
                q.setVertigini(rs.getShort("vertigini"));

                return q;
            }
        }
    }

}
