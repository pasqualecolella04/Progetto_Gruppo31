package stabilimento.database;

import stabilimento.entity.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {

    public boolean saveCliente(Cliente c) {
        String sql = "INSERT OR REPLACE INTO Utenti (idUtente, nome, cognome, email, telefono, passwordHash, ruolo, codiceFiscale) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getIdUtente());
                ps.setString(2, c.getNome());
                ps.setString(3, c.getCognome());
                ps.setString(4, c.getEmail());
                ps.setString(5, c.getTelefono());
                ps.setString(6, c.getPasswordHash());
                ps.setString(7, c.getRuolo());
                ps.setString(8, c.getCodiceFiscale());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del cliente: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public Cliente findClienteById(String idCliente) {
        String sql = "SELECT * FROM Utenti WHERE idUtente = ? AND ruolo = 'CLIENTE'";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Cliente(
                                rs.getString("idUtente"),
                                rs.getString("nome"),
                                rs.getString("cognome"),
                                rs.getString("email"),
                                rs.getString("telefono"),
                                rs.getString("passwordHash"),
                                rs.getString("codiceFiscale")
                        );
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella lettura del cliente: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }
}