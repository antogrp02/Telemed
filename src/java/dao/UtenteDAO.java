/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UtenteDAO {

    public static Utente findByUsername(String username) throws Exception {
        String sql = "SELECT id_utente, username FROM utente WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utente u = new Utente();
                u.setIdUtente(rs.getLong("id_utente"));
                u.setUsername(rs.getString("username"));
                return u;
            }
            return null;
        }
    }
    
        public static Utente insert(String username) throws Exception {
        String sql = "INSERT INTO utente (username) VALUES (?) RETURNING id_utente";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Utente u = new Utente();
                u.setIdUtente(rs.getLong("id_utente"));
                u.setUsername(username);
                return u;
            } else {
                throw new Exception("Impossibile creare utente");
            }
        }
    }

    public static Utente findById(long id) throws Exception {
        String sql = "SELECT id_utente, username FROM utente WHERE id_utente = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utente u = new Utente();
                u.setIdUtente(rs.getLong("id_utente"));
                u.setUsername(rs.getString("username"));
                return u;
            }
            return null;
        }
    }

    public static void delete(long id) throws Exception {
        String sql = "DELETE FROM utente WHERE id_utente = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

}
