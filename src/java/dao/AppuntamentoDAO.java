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
        String sql = "SELECT * " +
                     "FROM appuntamenti " +
                     "WHERE id_medico = ? " +
                     "  AND id_paziente = ? " +
                     "  AND data_ora >= NOW() " +
                     "ORDER BY data_ora ASC";

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
     * Inserisce un nuovo appuntamento.
     */
    public static void insert(Appuntamento a) throws Exception {
        String sql = "INSERT INTO appuntamenti (id_paziente, id_medico, tipo, data_ora) " +
                     "VALUES (?,?,?,?)";

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
