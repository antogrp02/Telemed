/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.ChatMessage;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageDAO {

    private static ChatMessage map(ResultSet rs) throws Exception {
        ChatMessage m = new ChatMessage();
        m.setIdMsg(rs.getLong("id_msg"));
        m.setIdMittente(rs.getLong("id_mittente"));
        m.setIdDestinatario(rs.getLong("id_destinatario"));
        m.setInviatoIl(rs.getTimestamp("inviato_il"));
        m.setTesto(rs.getString("testo"));
        return m;
    }

    public static void insert(ChatMessage m) throws Exception {
        String sql = "INSERT INTO chat_message " +
                     "(id_mittente, id_destinatario, inviato_il, testo) " +
                     "VALUES (?,?,?,?)";

        if (m.getInviatoIl() == null) {
            m.setInviatoIl(Timestamp.from(Instant.now()));
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, m.getIdMittente());
            ps.setLong(2, m.getIdDestinatario());
            ps.setTimestamp(3, m.getInviatoIl());
            ps.setString(4, m.getTesto());

            ps.executeUpdate();
        }
    }

    public static List<ChatMessage> getHistory(long u1, long u2) throws Exception {
        List<ChatMessage> list = new ArrayList<>();

        String sql = "SELECT * FROM chat_message " +
                     "WHERE (id_mittente = ? AND id_destinatario = ?) " +
                     "   OR (id_mittente = ? AND id_destinatario = ?) " +
                     "ORDER BY inviato_il ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, u1);
            ps.setLong(2, u2);
            ps.setLong(3, u2);
            ps.setLong(4, u1);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }
}
