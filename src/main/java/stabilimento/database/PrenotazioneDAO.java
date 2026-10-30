package stabilimento.database;

import stabilimento.entity.*;
import stabilimento.entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneDAO {
    private final UtenteDAO utenteDAO = new UtenteDAO();
    private final PostazioneDAO postazioneDAO = new PostazioneDAO();
    private final ServizioAggiuntivoDAO servizioDAO = new ServizioAggiuntivoDAO();

    public boolean save(Prenotazione p) {
        String sqlPrenotazione = """
            INSERT INTO Prenotazioni (idPrenotazione, dataCreazione, dataInizio, dataFine, oraInizio, stato, importoTotale, idCliente, idPostazione)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        String sqlServizi = """
            INSERT INTO ServiziPrenotati (idPrenotazione, idServizio, quantitaRichiesta, subTotale)
            VALUES (?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlPrenotazione)) {
                ps.setString(1, p.getIdPrenotazione());
                ps.setString(2, p.getDataCreazione().toString());
                ps.setString(3, p.getDataInizio().toString());
                ps.setString(4, p.getDataFine().toString());
                ps.setString(5, p.getOraInizio());
                ps.setString(6, p.getStato().name());
                ps.setDouble(7, p.getImportoTotale());
                ps.setString(8, p.getCliente().getIdUtente());
                ps.setString(9, p.getPostazioneScelta().getIdPostazione());
                ps.executeUpdate();
            }

            try (PreparedStatement psServ = conn.prepareStatement(sqlServizi)) {
                for (ServizioPrenotato sp : p.getServiziPrenotati()) {
                    psServ.setString(1, p.getIdPrenotazione());
                    psServ.setString(2, sp.getServizioRiferimento().getIdServizio());
                    psServ.setInt(3, sp.getQuantitaRichiesta());
                    psServ.setDouble(4, sp.getSubTotale());
                    psServ.addBatch();
                }
                psServ.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw new RuntimeException("Errore transazionale durante il salvataggio della prenotazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean checkConflittoPostazione(String idPostazione, LocalDate inizio, LocalDate fine) {
        String sql = """
            SELECT COUNT(*) FROM Prenotazioni
            WHERE idPostazione = ?
              AND stato != 'ANNULLATA'
              AND dataInizio <= ?
              AND dataFine >= ?
            """;
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idPostazione);
                ps.setString(2, fine.toString());
                ps.setString(3, inizio.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Errore verifica conflitto postazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public Prenotazione findByID(String idPrenotazione) {
        String sql = "SELECT * FROM Prenotazioni WHERE idPrenotazione = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idPrenotazione);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToPrenotazione(conn, rs);
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore findByID Prenotazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<Prenotazione> findByPeriodo(LocalDate inizio, LocalDate fine) {
        String sql = """
            SELECT * FROM Prenotazioni
            WHERE dataInizio <= ? AND dataFine >= ?
            ORDER BY dataInizio ASC
            """;
        List<Prenotazione> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fine.toString());
                ps.setString(2, inizio.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapRowToPrenotazione(conn, rs));
                    }
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Errore findByPeriodo Prenotazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<Prenotazione> findByCliente(String idCliente) {
        String sql = "SELECT * FROM Prenotazioni WHERE idCliente = ? ORDER BY dataInizio ASC";
        List<Prenotazione> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapRowToPrenotazione(conn, rs));
                    }
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Errore findByCliente Prenotazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean aggiornaStato(String idPrenotazione, StatoPrenotazione nuovoStato) {
        String sql = "UPDATE Prenotazioni SET stato = ? WHERE idPrenotazione = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nuovoStato.name());
                ps.setString(2, idPrenotazione);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento stato prenotazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    private Prenotazione mapRowToPrenotazione(Connection conn, ResultSet rs) throws SQLException {
        String idPren = rs.getString("idPrenotazione");
        LocalDate dataInizio = LocalDate.parse(rs.getString("dataInizio"));
        LocalDate dataFine = LocalDate.parse(rs.getString("dataFine"));
        String idCliente = rs.getString("idCliente");
        String idPostazione = rs.getString("idPostazione");

        Cliente cliente = utenteDAO.findClienteById(idCliente);
        Postazione postazione = postazioneDAO.findByID(idPostazione);

        Prenotazione p = new Prenotazione(idPren, dataInizio, dataFine, cliente, postazione);
        p.setDataCreazione(LocalDate.parse(rs.getString("dataCreazione")));
        p.setOraInizio(rs.getString("oraInizio"));
        p.setStato(StatoPrenotazione.valueOf(rs.getString("stato")));

        String sqlServ = "SELECT * FROM ServiziPrenotati WHERE idPrenotazione = ?";
        List<ServizioPrenotato> voci = new ArrayList<>();
        try (PreparedStatement psServ = conn.prepareStatement(sqlServ)) {
            psServ.setString(1, idPren);
            try (ResultSet rsServ = psServ.executeQuery()) {
                while (rsServ.next()) {
                    ServizioAggiuntivo sa = servizioDAO.findByID(rsServ.getString("idServizio"));
                    ServizioPrenotato sp = new ServizioPrenotato(sa, rsServ.getInt("quantitaRichiesta"));
                    sp.setSubTotale(rsServ.getDouble("subTotale"));
                    voci.add(sp);
                }
            }
        }
        p.setServiziPrenotati(voci);
        p.setImportoTotale(rs.getDouble("importoTotale"));
        return p;
    }
}