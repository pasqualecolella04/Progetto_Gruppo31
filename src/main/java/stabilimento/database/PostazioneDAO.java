package stabilimento.database;

import stabilimento.entity.Postazione;
import stabilimento.entity.Tariffa;
import stabilimento.entity.TipoPostazione;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PostazioneDAO {

    public boolean save(Postazione pos) {
        String sql = "INSERT OR REPLACE INTO Postazioni (idPostazione, numeroProgressivo, fila, posizione, occupata) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pos.getIdPostazione());
                ps.setInt(2, pos.getNumeroProgressivo());
                ps.setInt(3, pos.getFila());
                ps.setString(4, pos.getPosizione().name());
                ps.setInt(5, pos.isOccupata() ? 1 : 0);
                boolean esito = ps.executeUpdate() > 0;

                for (Tariffa t : pos.getListinoTariffe()) {
                    saveTariffa(conn, pos.getIdPostazione(), t);
                }
                return esito;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore salvataggio Postazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    private void saveTariffa(Connection conn, String idPostazione, Tariffa t) throws SQLException {
        String sql = "INSERT OR REPLACE INTO Tariffe (idTariffa, idPostazione, stagione, prezzoGiornaliero, dataInizioValidita, dataFineValidita) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getIdTariffa());
            ps.setString(2, idPostazione);
            ps.setString(3, t.getStagione());
            ps.setDouble(4, t.getPrezzoGiornaliero());
            ps.setString(5, t.getDataInizioValidita().toString());
            ps.setString(6, t.getDataFineValidita().toString());
            ps.executeUpdate();
        }
    }

    public Postazione findByID(String idPostazione) {
        String sql = "SELECT * FROM Postazioni WHERE idPostazione = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idPostazione);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Postazione p = mapRowToPostazione(rs);
                        p.setListinoTariffe(findTariffeByPostazione(conn, idPostazione));
                        return p;
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca Postazione per ID: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    /**
     * Restituisce tutte le postazioni che NON hanno prenotazioni attive (non annullate)
     * sovrapposte all'intervallo [inizio, fine].
     */
    public List<Postazione> findDisponibili(LocalDate inizio, LocalDate fine) {
        String sql = """
            SELECT * FROM Postazioni p
            WHERE p.idPostazione NOT IN (
                SELECT pr.idPostazione FROM Prenotazioni pr
                WHERE pr.stato != 'ANNULLATA'
                  AND pr.dataInizio <= ?
                  AND pr.dataFine >= ?
            )
            ORDER BY p.fila ASC, p.numeroProgressivo ASC
            """;
        List<Postazione> disponibili = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fine.toString());
                ps.setString(2, inizio.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Postazione p = mapRowToPostazione(rs);
                        p.setListinoTariffe(findTariffeByPostazione(conn, p.getIdPostazione()));
                        disponibili.add(p);
                    }
                }
            }
            return disponibili;
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca postazioni disponibili: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    private List<Tariffa> findTariffeByPostazione(Connection conn, String idPostazione) throws SQLException {
        String sql = "SELECT * FROM Tariffe WHERE idPostazione = ?";
        List<Tariffa> tariffe = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idPostazione);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tariffe.add(new Tariffa(
                            rs.getString("idTariffa"),
                            rs.getString("stagione"),
                            rs.getDouble("prezzoGiornaliero"),
                            LocalDate.parse(rs.getString("dataInizioValidita")),
                            LocalDate.parse(rs.getString("dataFineValidita"))
                    ));
                }
            }
        }
        return tariffe;
    }

    private Postazione mapRowToPostazione(ResultSet rs) throws SQLException {
        Postazione p = new Postazione(
                rs.getString("idPostazione"),
                rs.getInt("numeroProgressivo"),
                rs.getInt("fila"),
                TipoPostazione.valueOf(rs.getString("posizione"))
        );
        p.setOccupata(rs.getInt("occupata") == 1);
        return p;
    }
}