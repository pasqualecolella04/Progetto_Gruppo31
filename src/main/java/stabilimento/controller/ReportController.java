package stabilimento.controller;

import stabilimento.database.ReportDAO;
import stabilimento.dto.PrenotazioneDTO;
import stabilimento.dto.ReportDTO;
import stabilimento.entity.Prenotazione;
import stabilimento.entity.Report;
import stabilimento.entity.TipoReport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportController {
    private static ReportController instance;
    private final ReportDAO reportDAO;

    private ReportController() {
        this.reportDAO = new ReportDAO();
    }

    public static synchronized ReportController getInstance() {
        if (instance == null) {
            instance = new ReportController();
        }
        return instance;
    }

    public ReportDTO generaReport(TipoReport tipoReport, LocalDate dataInizio, LocalDate dataFine, String idCliente) {
        if (tipoReport == null) {
            throw new IllegalArgumentException("Selezionare una tipologia di report valida.");
        }

        List<Prenotazione> prenotazioniEstratte;

        if (tipoReport == TipoReport.SETTIMANALE || tipoReport == TipoReport.MENSILE) {
            if (dataInizio == null || dataFine == null) {
                throw new IllegalArgumentException("Le date di inizio e fine sono obbligatorie per i report periodici.");
            }
            if (dataInizio.isAfter(dataFine)) {
                throw new IllegalArgumentException("Intervallo di date inserito non valido: data inizio successiva a data fine.");
            }
            prenotazioniEstratte = reportDAO.estraiPrenotazioniPerPeriodo(dataInizio, dataFine);
        } else if (tipoReport == TipoReport.CLIENTE) {
            if (idCliente == null || idCliente.isBlank()) {
                throw new IllegalArgumentException("L'identificativo del cliente è obbligatorio per il Report Cliente.");
            }
            prenotazioniEstratte = reportDAO.estraiPrenotazioniPerCliente(idCliente);
        } else {
            throw new IllegalArgumentException("Tipologia di report non supportata.");
        }

        // Gestione Scenario Alternativo 4.1: Nessuna prenotazione trovata nel DB
        if (prenotazioniEstratte == null || prenotazioniEstratte.isEmpty()) {
            throw new NoSuchElementException("Nessuna prenotazione trovata per i criteri specificati");
        }

        String idReport = "REP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Report reportEntity = new Report(idReport, tipoReport, dataInizio, dataFine, idCliente);
        reportEntity.calcolaTotaliEIndiciAffluenza(prenotazioniEstratte);

        List<PrenotazioneDTO> dettagliDTO = prenotazioniEstratte.stream()
                .map(PrenotazioneController.getInstance()::toDTO)
                .collect(Collectors.toList());

        return new ReportDTO(
                reportEntity.getIdReport(),
                reportEntity.getTipoReport().name(),
                reportEntity.getDataInizio(),
                reportEntity.getDataFine(),
                reportEntity.getIdClienteFiltro(),
                reportEntity.getNumeroTotalePrenotazioni(),
                reportEntity.getIncassoTotale(),
                reportEntity.getIncassoMedioPerPrenotazione(),
                reportEntity.getTotaleServiziAccessoriVenduti(),
                dettagliDTO
        );
    }

    public File esportaReport(ReportDTO reportDTO, String formato) {
        if (reportDTO == null) {
            throw new IllegalArgumentException("I dati del report da esportare non possono essere nulli.");
        }
        if (formato == null || (!formato.equalsIgnoreCase("CSV") && !formato.equalsIgnoreCase("PDF"))) {
            throw new IllegalArgumentException("Formato di esportazione richiesto non supportato (formati ammessi: PDF, CSV)");
        }

        String estensione = formato.toLowerCase();
        File outputFile = new File("report_" + reportDTO.getIdReport() + "." + estensione);

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("ID Report;Tipo;Prenotazioni Totali;Incasso Totale (EUR);Incasso Medio (EUR);Servizi Venduti\n");
            writer.write(String.format("%s;%s;%d;%.2f;%.2f;%d\n\n",
                    reportDTO.getIdReport(),
                    reportDTO.getTipoReport(),
                    reportDTO.getNumeroTotalePrenotazioni(),
                    reportDTO.getIncassoTotale(),
                    reportDTO.getIncassoMedioPerPrenotazione(),
                    reportDTO.getTotaleServiziAccessoriVenduti()));

            writer.write("ID Prenotazione;Cliente;Postazione;Data Inizio;Data Fine;Stato;Importo (EUR)\n");
            for (PrenotazioneDTO p : reportDTO.getDettaglioPrenotazioni()) {
                writer.write(String.format("%s;%s;%s;%s;%s;%s;%.2f\n",
                        p.getIdPrenotazione(),
                        p.getNominativoCliente(),
                        p.getIdPostazione(),
                        p.getDataInizio(),
                        p.getDataFine(),
                        p.getStato(),
                        p.getImportoTotale()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore durante l'esportazione del file report: " + e.getMessage(), e);
        }
        return outputFile;
    }
}