/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
import java.time.LocalDate;

public class DailyDAO {

    public static void setParametriOk(long idPaz, LocalDate day) {
        String sql = """
            INSERT INTO giornata_monitoraggio (id_paziente, giorno, parametri_ok)
            VALUES (?, ?, TRUE)
            ON CONFLICT (id_paziente, giorno)
            DO UPDATE SET parametri_ok = TRUE
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ps.setDate(2, Date.valueOf(day));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setQuestionarioOk(long idPaz, LocalDate day) {
        String sql = """
            INSERT INTO giornata_monitoraggio (id_paziente, giorno, questionario_ok)
            VALUES (?, ?, TRUE)
            ON CONFLICT (id_paziente, giorno)
            DO UPDATE SET questionario_ok = TRUE
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ps.setDate(2, Date.valueOf(day));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** true se parametri_ok && questionario_ok && !predizione_fatta */
    public static boolean canPredict(long idPaz, LocalDate day) {
        String sql = """
            SELECT parametri_ok, questionario_ok, predizione_fatta
            FROM giornata_monitoraggio
            WHERE id_paziente = ? AND giorno = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ps.setDate(2, Date.valueOf(day));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean paramOk  = rs.getBoolean("parametri_ok");
                    boolean questOk  = rs.getBoolean("questionario_ok");
                    boolean done     = rs.getBoolean("predizione_fatta");
                    return paramOk && questOk && !done;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void markPredizioneFatta(long idPaz, LocalDate day) {
        String sql = """
            UPDATE giornata_monitoraggio
            SET predizione_fatta = TRUE
            WHERE id_paziente = ? AND giorno = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPaz);
            ps.setDate(2, Date.valueOf(day));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

