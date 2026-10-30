package stabilimento.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    private static DBManager instance;
    private String url = "jdbc:sqlite:stabilimento_gruppo31.db";

    private DBManager() {
        inizializzaSchemaDatabase();
    }

    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    /**
     * Consente di commutare sul database SQLite in-memory o file di test durante l'esecuzione di JUnit.
     */
    public void setDatabaseUrl(String customUrl) {
        this.url = customUrl;
        inizializzaSchemaDatabase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Errore durante la chiusura della connessione JDBC: " + e.getMessage());
            }
        }
    }

    public void inizializzaSchemaDatabase() {
        String sqlUtenti = """
            CREATE TABLE IF NOT EXISTS Utenti (
                idUtente TEXT PRIMARY KEY,
                nome TEXT NOT NULL,
                cognome TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                telefono TEXT,
                passwordHash TEXT NOT NULL,
                ruolo TEXT NOT NULL,
                codiceFiscale TEXT
            );
            """;

        String sqlPostazioni = """
            CREATE TABLE IF NOT EXISTS Postazioni (
                idPostazione TEXT PRIMARY KEY,
                numeroProgressivo INTEGER NOT NULL,
                fila INTEGER NOT NULL,
                posizione TEXT NOT NULL,
                occupata INTEGER DEFAULT 0
            );
            """;

        String sqlTariffe = """
            CREATE TABLE IF NOT EXISTS Tariffe (
                idTariffa TEXT PRIMARY KEY,
                idPostazione TEXT NOT NULL,
                stagione TEXT NOT NULL,
                prezzoGiornaliero REAL NOT NULL,
                dataInizioValidita TEXT NOT NULL,
                dataFineValidita TEXT NOT NULL,
                FOREIGN KEY (idPostazione) REFERENCES Postazioni(idPostazione)
            );
            """;

        String sqlServizi = """
            CREATE TABLE IF NOT EXISTS ServiziAggiuntivi (
                idServizio TEXT PRIMARY KEY,
                nomeServizio TEXT NOT NULL,
                descrizione TEXT,
                disponibilitaMax INTEGER NOT NULL,
                prezzoBaseGiornaliero REAL NOT NULL
            );
            """;

        String sqlPrenotazioni = """
            CREATE TABLE IF NOT EXISTS Prenotazioni (
                idPrenotazione TEXT PRIMARY KEY,
                dataCreazione TEXT NOT NULL,
                dataInizio TEXT NOT NULL,
                dataFine TEXT NOT NULL,
                oraInizio TEXT,
                stato TEXT NOT NULL,
                importoTotale REAL NOT NULL,
                idCliente TEXT NOT NULL,
                idPostazione TEXT NOT NULL,
                FOREIGN KEY (idCliente) REFERENCES Utenti(idUtente),
                FOREIGN KEY (idPostazione) REFERENCES Postazioni(idPostazione)
            );
            """;

        String sqlServiziPrenotati = """
            CREATE TABLE IF NOT EXISTS ServiziPrenotati (
                idPrenotazione TEXT NOT NULL,
                idServizio TEXT NOT NULL,
                quantitaRichiesta INTEGER NOT NULL,
                subTotale REAL NOT NULL,
                PRIMARY KEY (idPrenotazione, idServizio),
                FOREIGN KEY (idPrenotazione) REFERENCES Prenotazioni(idPrenotazione),
                FOREIGN KEY (idServizio) REFERENCES ServiziAggiuntivi(idServizio)
            );
            """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlUtenti);
                stmt.execute(sqlPostazioni);
                stmt.execute(sqlTariffe);
                stmt.execute(sqlServizi);
                stmt.execute(sqlPrenotazioni);
                stmt.execute(sqlServiziPrenotati);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'inizializzazione dello schema DB: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
    }
}