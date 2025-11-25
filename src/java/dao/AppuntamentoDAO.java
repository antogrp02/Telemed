/*
 * DAO per la tabella appuntamenti
 */
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Appuntamento;

public class AppuntamentoDAO {

    private static Appuntamento map(ResultSet rs) throws SQLException {
        Appuntamento a = new Appuntamento();
        a.setId(rs.getLong("id"));
        a.setIdPaziente(rs.getLong("id_paziente"));
        a.setIdMedico(rs.getLong("id_medico"));
        a.setTipo(rs.getString("tipo"));
        a.setDataOra(rs.getTimestamp("data_ora"));
        return a;
    }

    /**
     * Ritorna tutti gli appuntamenti FUTURI (o di oggi dopo l'ora corrente)
     * per la coppia medico–paziente.
     */
    public static List<Appuntamento> getFuturiByMedicoAndPaziente(long idMedico, long idPaziente) throws Exception {
        String sql = "SELECT * "
                   + "FROM appuntamenti "
                   + "WHERE id_medico = ? "
                   + "  AND id_paziente = ? "
                   + "  AND data_ora >= NOW() "
                   + "ORDER BY data_ora ASC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, idMedico);
            ps.setLong(2, idPaziente);

            try (ResultSet rs = ps.executeQuery()) {
                List<Appuntamento> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    /**
     * Ritorna tutti gli appuntamenti FUTURI per un dato medico,
     * con paginazione (page >= 1).
     */
    public static List<Appuntamento> getFuturiByMedico(long idMedico, int page, int pageSize) throws Exception {
        if (page < 1) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        int offset = (page - 1) * pageSize;

        String sql = "SELECT * "
                   + "FROM appuntamenti "
                   + "WHERE id_medico = ? "
                   + "  AND data_ora >= NOW() "
                   + "ORDER BY data_ora ASC "
                   + "LIMIT ? OFFSET ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, idMedico);
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                List<Appuntamento> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    /**
     * Conta tutti gli appuntamenti FUTURI per un dato medico.
     */
    public static int countFuturiByMedico(long idMedico) throws Exception {
        String sql = "SELECT COUNT(*) AS cnt "
                   + "FROM appuntamenti "
                   + "WHERE id_medico = ? "
                   + "  AND data_ora >= NOW()";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, idMedico);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
                return 0;
            }
        }
    }

    /**
     * Aggiorna la data/ora di un appuntamento (rinvio).
     */
    public static void updateDataOra(long idAppuntamento, Timestamp nuovaDataOra) throws Exception {
        String sql = "UPDATE appuntamenti "
                   + "SET data_ora = ? "
                   + "WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, nuovaDataOra);
            ps.setLong(2, idAppuntamento);

            ps.executeUpdate();
        }
    }

    /**
     * Elimina un appuntamento (annullamento).
     */
    public static void delete(long idAppuntamento) throws Exception {
        String sql = "DELETE FROM appuntamenti WHERE id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, idAppuntamento);
            ps.executeUpdate();
        }
    }

    /**
     * Inserisce un nuovo appuntamento.
     */
    public static void insert(Appuntamento a) throws Exception {
        String sql = "INSERT INTO appuntamenti (id_paziente, id_medico, tipo, data_ora) "
                   + "VALUES (?,?,?,?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, a.getIdPaziente());
            ps.setLong(2, a.getIdMedico());
            ps.setString(3, a.getTipo());
            ps.setTimestamp(4, a.getDataOra());

            ps.executeUpdate();
        }
    }
}
