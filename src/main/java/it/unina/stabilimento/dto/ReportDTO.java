package it.unina.stabilimento.dto;

import java.time.LocalDate;
import java.util.List;

public class ReportDTO {
    private final String idReport;
    private final String tipoReport;
    private final LocalDate dataInizio;
    private final LocalDate dataFine;
    private final String idClienteFiltro;
    private final int numeroTotalePrenotazioni;
    private final double incassoTotale;
    private final double incassoMedioPerPrenotazione;
    private final int totaleServiziAccessoriVenduti;
    private final List<PrenotazioneDTO> dettaglioPrenotazioni;

    public ReportDTO(String idReport, String tipoReport, LocalDate dataInizio, LocalDate dataFine,
                     String idClienteFiltro, int numeroTotalePrenotazioni, double incassoTotale,
                     double incassoMedioPerPrenotazione, int totaleServiziAccessoriVenduti,
                     List<PrenotazioneDTO> dettaglioPrenotazioni) {
        this.idReport = idReport;
        this.tipoReport = tipoReport;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.idClienteFiltro = idClienteFiltro;
        this.numeroTotalePrenotazioni = numeroTotalePrenotazioni;
        this.incassoTotale = incassoTotale;
        this.incassoMedioPerPrenotazione = incassoMedioPerPrenotazione;
        this.totaleServiziAccessoriVenduti = totaleServiziAccessoriVenduti;
        this.dettaglioPrenotazioni = dettaglioPrenotazioni;
    }

    public String getIdReport() { return idReport; }
    public String getTipoReport() { return tipoReport; }
    public LocalDate getDataInizio() { return dataInizio; }
    public LocalDate getDataFine() { return dataFine; }
    public String getIdClienteFiltro() { return idClienteFiltro; }
    public int getNumeroTotalePrenotazioni() { return numeroTotalePrenotazioni; }
    public double getIncassoTotale() { return incassoTotale; }
    public double getIncassoMedioPerPrenotazione() { return incassoMedioPerPrenotazione; }
    public int getTotaleServiziAccessoriVenduti() { return totaleServiziAccessoriVenduti; }
    public List<PrenotazioneDTO> getDettaglioPrenotazioni() { return dettaglioPrenotazioni; }
}