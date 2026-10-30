package stabilimento.controller;

import stabilimento.database.*;
import stabilimento.database.DBManager;
import stabilimento.database.PostazioneDAO;
import stabilimento.database.ServizioAggiuntivoDAO;
import stabilimento.database.UtenteDAO;
import stabilimento.dto.PrenotazioneDTO;
import stabilimento.entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stabilimento.entity.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PrenotazioneControllerTest {

    private PrenotazioneController controller;
    private UtenteDAO utenteDAO;
    private PostazioneDAO postazioneDAO;
    private ServizioAggiuntivoDAO servizioDAO;

    @BeforeEach
    void setUp() throws Exception {
        DBManager.getInstance().setDatabaseUrl("jdbc:sqlite:test_stabilimento.db");
        puliziaTabelle();

        controller = PrenotazioneController.getInstance();
        utenteDAO = new UtenteDAO();
        postazioneDAO = new PostazioneDAO();
        servizioDAO = new ServizioAggiuntivoDAO();

        // Popolamento dati di fixture
        Cliente cliente = new Cliente("C102", "Mario", "Rossi", "mario.rossi@unina.it", "3331234567", "hash123", "RSSMRA80A01F839X");
        utenteDAO.saveCliente(cliente);

        Postazione p1 = new Postazione("POS-01", 1, 1, TipoPostazione.PRIMA_FILA);
        p1.aggiungiTariffa(new Tariffa("TAR-AGO", "Alta Stagione", 35.0,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)));
        postazioneDAO.save(p1);

        ServizioAggiuntivo lettino = new ServizioAggiuntivo("S01", "Lettino Extra", "Lettino aggiuntivo in alluminio", 5, 10.0);
        ServizioAggiuntivo cabina = new ServizioAggiuntivo("S02", "Cabina Privata", "Cabina spogliatoio con doccia", 2, 15.0);
        servizioDAO.save(lettino);
        servizioDAO.save(cabina);
    }

    @AfterEach
    void tearDown() throws Exception {
        puliziaTabelle();
    }

    private void puliziaTabelle() throws Exception {
        Connection conn = DBManager.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM ServiziPrenotati");
            stmt.execute("DELETE FROM Prenotazioni");
            stmt.execute("DELETE FROM Tariffe");
            stmt.execute("DELETE FROM ServiziAggiuntivi");
            stmt.execute("DELETE FROM Postazioni");
            stmt.execute("DELETE FROM Utenti");
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    @Test
    void testEffettuaPrenotazioneNominaleConServiziECalcoloImporto() {
        // 3 giorni in Alta Stagione (01/08 - 03/08):
        // Postazione POS-01: 35.0 EUR * 3 gg = 105.0 EUR
        // 2 Lettini Extra (S01): (10.0 EUR * 2) * 3 gg = 60.0 EUR
        // 1 Cabina (S02): (15.0 EUR * 1) * 3 gg = 45.0 EUR
        // Totale atteso = 210.0 EUR
        Map<String, Integer> servizi = new HashMap<>();
        servizi.put("S01", 2);
        servizi.put("S02", 1);

        LocalDate inizio = LocalDate.of(2026, 8, 1);
        LocalDate fine = LocalDate.of(2026, 8, 3);

        PrenotazioneDTO risultato = controller.effettuaPrenotazione("C102", "POS-01", servizi, inizio, fine);

        assertNotNull(risultato);
        assertEquals("C102", risultato.getIdCliente());
        assertEquals("POS-01", risultato.getIdPostazione());
        assertEquals(3L, risultato.getGiorniPermanenza());
        assertEquals(3, risultato.getNumeroServiziAccessori());
        assertEquals(210.0, risultato.getImportoTotale(), 0.001);
        assertEquals("IN_ATTESA_DI_CONFERMA", risultato.getStato());
    }

    @Test
    void testErroreDataInizioSuccessivaADataFine() {
        LocalDate inizioErrato = LocalDate.of(2026, 8, 10);
        LocalDate fineErrata = LocalDate.of(2026, 8, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                controller.effettuaPrenotazione("C102", "POS-01", null, inizioErrato, fineErrata)
        );
        assertTrue(ex.getMessage().contains("successiva"));
    }

    @Test
    void testErrorePostazioneGiaOccupataNelPeriodo() {
        LocalDate inizio1 = LocalDate.of(2026, 8, 1);
        LocalDate fine1 = LocalDate.of(2026, 8, 5);
        controller.effettuaPrenotazione("C102", "POS-01", null, inizio1, fine1);

        // Tentativo di prenotazione sovrapposta (03/08 - 07/08) sulla medesima postazione POS-01
        LocalDate inizioSovrapposto = LocalDate.of(2026, 8, 3);
        LocalDate fineSovrapposta = LocalDate.of(2026, 8, 7);

        assertThrows(IllegalStateException.class, () ->
                controller.effettuaPrenotazione("C102", "POS-01", null, inizioSovrapposto, fineSovrapposta)
        );

        List<Postazione> libere = controller.visualizzaPostazioniDisponibili(inizioSovrapposto, fineSovrapposta);
        assertTrue(libere.isEmpty());
    }

    @Test
    void testErroreQuantitaServizioSuperioreDisponibilitaMax() {
        Map<String, Integer> serviziEccessivi = new HashMap<>();
        serviziEccessivi.put("S02", 10); // Disponibilità max cabina = 2

        assertThrows(IllegalArgumentException.class, () ->
                controller.effettuaPrenotazione("C102", "POS-01", serviziEccessivi,
                        LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2))
        );
    }
}