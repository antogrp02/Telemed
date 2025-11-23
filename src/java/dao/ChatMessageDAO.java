package dao;

import model.ChatMessage;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMessageDAO {

    private static ChatMessage map(ResultSet rs) throws Exception {
        ChatMessage m = new ChatMessage();
        m.setIdMsg(rs.getLong("id_msg"));
        m.setIdMittente(rs.getLong("id_mittente"));
        m.setIdDestinatario(rs.getLong("id_destinatario"));
        m.setInviatoIl(rs.getTimestamp("inviato_il"));
        m.setTesto(rs.getString("testo"));
        m.setLetto(rs.getBoolean("letto"));
        return m;
    }

    // ---------------------------------------------------------
    // INSERT NUOVO MESSAGGIO (letto = false di default)
    // ---------------------------------------------------------
    public static void insert(ChatMessage m) throws Exception {
        String sql = "INSERT INTO chat_message " +
                "(id_mittente, id_destinatario, inviato_il, testo, letto) " +
                "VALUES (?,?,?,?, FALSE)";

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

    // ---------------------------------------------------------
    // CRONOLOGIA CHAT
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // SEGNARE COME LETTI TUTTI I MESSAGGI DI UN PAZIENTE → MEDICO
    // ---------------------------------------------------------
    public static void segnaComeLetti(long idPaziente, long idMedico) throws Exception {
        String sql = "UPDATE chat_message " +
                "SET letto = TRUE " +
                "WHERE id_mittente = ? AND id_destinatario = ? AND letto = FALSE";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPaziente);
            ps.setLong(2, idMedico);
            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------
    // CONTEGGIO MESSAGGI NON LETTI PER OGNI PAZIENTE
    // ---------------------------------------------------------
    public static Map<Long, Integer> getUnreadMessagesByPatient(long idMedico) throws Exception {
        Map<Long, Integer> result = new HashMap<>();

        String sql = """
            SELECT id_mittente AS paziente, COUNT(*) AS unread
            FROM chat_message
            WHERE id_destinatario = ? AND letto = FALSE
            GROUP BY id_mittente
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idMedico);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getLong("paziente"), rs.getInt("unread"));
            }
        }
        return result;
    }

    // ---------------------------------------------------------
    // SEGNARE UN SINGOLO MESSAGGIO COME LETTO (realtime WS)
    // ---------------------------------------------------------
    public static void segnaUltimoComeLetto(long idPaziente, long idMedico) throws Exception {
        String sql = """
            UPDATE chat_message
            SET letto = TRUE
            WHERE id_mittente = ? 
              AND id_destinatario = ?
              AND letto = FALSE
            ORDER BY inviato_il DESC
            LIMIT 1
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPaziente);
            ps.setLong(2, idMedico);
            ps.executeUpdate();
        }
    }
}
