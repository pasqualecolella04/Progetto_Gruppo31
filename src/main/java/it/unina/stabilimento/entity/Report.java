package it.unina.stabilimento.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Report {
    private String idReport;
    private TipoReport tipoReport;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private String idClienteFiltro;
    private int numeroTotalePrenotazioni;
    private double incassoTotale;
    private double incassoMedioPerPrenotazione;
    private int totaleServiziAccessoriVenduti;

    public Report(String idReport, TipoReport tipoReport, LocalDate dataInizio, LocalDate dataFine, String idClienteFiltro) {
        this.idReport = idReport;
        this.tipoReport = tipoReport;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.idClienteFiltro = idClienteFiltro;
    }

    /**
     * Regola di business per UC12 (Genera Report): aggrega gli incassi, conta le prenotazioni
     * valide (non annullate) e calcola gli indici medi e il volume dei servizi accessori.
     */
    public void calcolaTotaliEIndiciAffluenza(List<Prenotazione> prenotazioni) {
        this.numeroTotalePrenotazioni = 0;
        this.incassoTotale = 0.0;
        this.totaleServiziAccessoriVenduti = 0;

        if (prenotazioni == null || prenotazioni.isEmpty()) {
            this.incassoMedioPerPrenotazione = 0.0;
            return;
        }

        for (Prenotazione p : prenotazioni) {
            if (p.getStato() != StatoPrenotazione.ANNULLATA) {
                this.numeroTotalePrenotazioni++;
                this.incassoTotale += p.getImportoTotale();
                for (ServizioPrenotato sp : p.getServiziPrenotati()) {
                    this.totaleServiziAccessoriVenduti += sp.getQuantitaRichiesta();
                }
            }
        }

        this.incassoMedioPerPrenotazione = (this.numeroTotalePrenotazioni > 0)
                ? (this.incassoTotale / this.numeroTotalePrenotazioni)
                : 0.0;
    }

    public String getIdReport() { return idReport; }
    public TipoReport getTipoReport() { return tipoReport; }
    public LocalDate getDataInizio() { return dataInizio; }
    public LocalDate getDataFine() { return dataFine; }
    public String getIdClienteFiltro() { return idClienteFiltro; }
    public int getNumeroTotalePrenotazioni() { return numeroTotalePrenotazioni; }
    public double getIncassoTotale() { return incassoTotale; }
    public double getIncassoMedioPerPrenotazione() { return incassoMedioPerPrenotazione; }
    public int getTotaleServiziAccessoriVenduti() { return totaleServiziAccessoriVenduti; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(idReport, report.idReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReport);
    }
}