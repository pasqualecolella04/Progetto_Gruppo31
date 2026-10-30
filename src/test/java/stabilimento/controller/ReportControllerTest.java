package stabilimento.controller;

import stabilimento.database.*;
import stabilimento.database.DBManager;
import stabilimento.database.PostazioneDAO;
import stabilimento.database.ServizioAggiuntivoDAO;
import stabilimento.database.UtenteDAO;
import stabilimento.dto.ReportDTO;
import stabilimento.entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stabilimento.entity.*;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class ReportControllerTest {

    private ReportController reportController;
    private PrenotazioneController prenotazioneController;
    private File fileEsportatoTest;

    @BeforeEach
    void setUp() throws Exception {
        DBManager.getInstance().setDatabaseUrl("jdbc:sqlite:test_stabilimento.db");
        puliziaTabelle();

        reportController = ReportController.getInstance();
        prenotazioneController = PrenotazioneController.getInstance();

        UtenteDAO utenteDAO = new UtenteDAO();
        PostazioneDAO postazioneDAO = new PostazioneDAO();
        ServizioAggiuntivoDAO servizioDAO = new ServizioAggiuntivoDAO();

        Cliente c1 = new Cliente("C102", "Mario", "Rossi", "mario@unina.it", "333111", "pwd", "CF1");
        utenteDAO.saveCliente(c1);

        Postazione pos1 = new Postazione("POS-01", 1, 1, TipoPostazione.PRIMA_FILA); // 30.0 EUR/gg base
        Postazione pos2 = new Postazione("POS-02", 2, 2, TipoPostazione.SECONDA_FILA); // 25.0 EUR/gg base
        postazioneDAO.save(pos1);
        postazioneDAO.save(pos2);

        ServizioAggiuntivo s1 = new ServizioAggiuntivo("S01", "Lettino Extra", "Lettino", 10, 10.0);
        servizioDAO.save(s1);

        // Prenotazione 1: 2 giorni su POS-01 (30*2 = 60 EUR) + 1 lettino per 2 gg (20 EUR) = 80.0 EUR
        Map<String, Integer> serv = new HashMap<>();
        serv.put("S01", 1);
        prenotazioneController.effettuaPrenotazione("C102", "POS-01", serv,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2));

        // Prenotazione 2: 2 giorni su POS-02 (25*2 = 50.0 EUR) senza servizi extra
        prenotazioneController.effettuaPrenotazione("C102", "POS-02", null,
                LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 5));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (fileEsportatoTest != null && fileEsportatoTest.exists()) {
            fileEsportatoTest.delete();
        }
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
    void testGeneraReportSettimanaleConDatiPresentiEdEsportazioneCSV() {
        // TC02: Report Settimanale dal 01/08/2026 al 07/08/2026
        // Incasso totale atteso = 80.0 + 50.0 = 130.0 EUR su 2 prenotazioni (Media = 65.0 EUR)
        ReportDTO report = reportController.generaReport(
                TipoReport.SETTIMANALE,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7),
                null
        );

        assertNotNull(report);
        assertEquals(2, report.getNumeroTotalePrenotazioni());
        assertEquals(130.0, report.getIncassoTotale(), 0.001);
        assertEquals(65.0, report.getIncassoMedioPerPrenotazione(), 0.001);
        assertEquals(1, report.getTotaleServiziAccessoriVenduti());

        fileEsportatoTest = reportController.esportaReport(report, "CSV");
        assertNotNull(fileEsportatoTest);
        assertTrue(fileEsportatoTest.exists());
    }

    @Test
    void testGeneraReportClienteConDatiPresenti() {
        // TC07/TC09: Report Cliente per C102
        ReportDTO report = reportController.generaReport(TipoReport.CLIENTE, null, null, "C102");

        assertNotNull(report);
        assertEquals("CLIENTE", report.getTipoReport());
        assertEquals(2, report.getNumeroTotalePrenotazioni());
        assertEquals(130.0, report.getIncassoTotale(), 0.001);
    }

    @Test
    void testErroreNessunaPrenotazioneTrovataNelPeriodo() {
        // TC10: Scenario alternativo 4.1 (Periodo 01/01/2026 - 07/01/2026 senza prenotazioni)
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                reportController.generaReport(
                        TipoReport.SETTIMANALE,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 7),
                        null
                )
        );
        assertEquals("Nessuna prenotazione trovata per i criteri specificati", ex.getMessage());
    }

    @Test
    void testErroreParametriReportNulliOIntervalloDateInvalido() {
        // TC13: TipoReport nullo
        assertThrows(IllegalArgumentException.class, () ->
                reportController.generaReport(null, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 7), null)
        );

        // TC14: Data Inizio > Data Fine (15/08/2026 > 01/08/2026)
        assertThrows(IllegalArgumentException.class, () ->
                reportController.generaReport(
                        TipoReport.SETTIMANALE,
                        LocalDate.of(2026, 8, 15),
                        LocalDate.of(2026, 8, 1),
                        null
                )
        );

        // TC15: Formato di esportazione non supportato (.exe)
        ReportDTO reportValido = reportController.generaReport(
                TipoReport.SETTIMANALE,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7),
                null
        );
        assertThrows(IllegalArgumentException.class, () ->
                reportController.esportaReport(reportValido, ".exe")
        );
    }
}