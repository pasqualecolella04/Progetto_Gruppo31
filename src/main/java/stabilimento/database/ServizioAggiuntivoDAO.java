package stabilimento.database;

import stabilimento.entity.ServizioAggiuntivo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServizioAggiuntivoDAO {

    public boolean save(ServizioAggiuntivo s) {
        String sql = "INSERT OR REPLACE INTO ServiziAggiuntivi (idServizio, nomeServizio, descrizione, disponibilitaMax, prezzoBaseGiornaliero) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, s.getIdServizio());
                ps.setString(2, s.getNomeServizio());
                ps.setString(3, s.getDescrizione());
                ps.setInt(4, s.getDisponibilitaMax());
                ps.setDouble(5, s.getPrezzoBaseGiornaliero());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore salvataggio ServizioAggiuntivo: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public ServizioAggiuntivo findByID(String idServizio) {
        String sql = "SELECT * FROM ServiziAggiuntivi WHERE idServizio = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idServizio);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new ServizioAggiuntivo(
                                rs.getString("idServizio"),
                                rs.getString("nomeServizio"),
                                rs.getString("descrizione"),
                                rs.getInt("disponibilitaMax"),
                                rs.getDouble("prezzoBaseGiornaliero")
                        );
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore lettura ServizioAggiuntivo: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<ServizioAggiuntivo> findAll() {
        String sql = "SELECT * FROM ServiziAggiuntivi";
        List<ServizioAggiuntivo> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ServizioAggiuntivo(
                            rs.getString("idServizio"),
                            rs.getString("nomeServizio"),
                            rs.getString("descrizione"),
                            rs.getInt("disponibilitaMax"),
                            rs.getDouble("prezzoBaseGiornaliero")
                    ));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Errore findAll ServiziAggiuntivi: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }
}