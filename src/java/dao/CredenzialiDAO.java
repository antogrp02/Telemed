/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CredenzialiDAO {

    public static class Credenziale {
        public String username;
        public String password;
        public int role;  // 0=paziente, 1=medico, 2=admin
    }

    public static Credenziale checkLogin(String username, String password) throws Exception {
        String sql = "SELECT \"Username\", \"Password\", \"Role\" " +
                     "FROM credenziali WHERE \"Username\" = ? AND \"Password\" = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            Credenziale c = new Credenziale();
            c.username = rs.getString("Username");
            c.password = rs.getString("Password");
            c.role     = rs.getInt("Role");
            return c;
        }
    }
    
        public static void insert(String username, String password, int role) throws Exception {
        String sql = "INSERT INTO credenziali(\"Username\", \"Password\", \"Role\") VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setInt(3, role);
            ps.executeUpdate();
        }
    }

    public static void deleteByUsername(String username) throws Exception {
        String sql = "DELETE FROM credenziali WHERE \"Username\" = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.executeUpdate();
        }
    }
    
        public static void updatePassword(String username, String newPassword) throws Exception {
        String sql = "UPDATE credenziali SET \"Password\" = ? WHERE \"Username\" = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);  // per ora mantieni in chiaro come il resto del progetto
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }


}
