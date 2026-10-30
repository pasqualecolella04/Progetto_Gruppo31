package stabilimento.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Prenotazione {
    private String idPrenotazione;
    private LocalDate dataCreazione;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private String oraInizio;
    private StatoPrenotazione stato;
    private double importoTotale;
    private Cliente cliente;
    private Postazione postazioneScelta;
    private List<ServizioPrenotato> serviziPrenotati;

    public Prenotazione(String idPrenotazione, LocalDate dataInizio, LocalDate dataFine, Cliente cliente, Postazione postazioneScelta) {
        if (dataInizio == null || dataFine == null || dataInizio.isAfter(dataFine)) {
            throw new IllegalArgumentException("Intervallo di date non valido: la data di inizio non può essere successiva alla data di fine.");
        }
        this.idPrenotazione = idPrenotazione;
        this.dataCreazione = LocalDate.now();
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.oraInizio = "09:00";
        this.stato = StatoPrenotazione.IN_ATTESA_DI_CONFERMA;
        this.cliente = cliente;
        this.postazioneScelta = postazioneScelta;
        this.serviziPrenotati = new ArrayList<>();
        this.importoTotale = calcolaImportoTotale();
    }

    /**
     * Calcola il numero di giorni di permanenza inclusivi (es. dal 01/08 al 03/08 sono 3 giorni).
     */
    public long calcolaGiorniPermanenza() {
        return ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;
    }

    public void aggiungiServizioPrenotato(ServizioAggiuntivo s, int quantita) {
        if (s == null || quantita <= 0) {
            throw new IllegalArgumentException("Servizio o quantità non validi.");
        }
        if (!s.verificaDisponibilita(quantita)) {
            throw new IllegalArgumentException("Quantità richiesta superiore alla disponibilità massima per il servizio: " + s.getNomeServizio());
        }
        ServizioPrenotato sp = new ServizioPrenotato(s, quantita);
        sp.calcolaSubTotale(this.dataInizio, this.dataFine);
        this.serviziPrenotati.add(sp);
        calcolaImportoTotale();
    }

    /**
     * Regola di business (Information Expert): calcola l'importo totale sommando
     * il costo giornaliero della postazione per ogni giorno del periodo e i subtotali dei servizi aggiuntivi.
     */
    public double calcolaImportoTotale() {
        double totalePostazione = 0.0;
        LocalDate curr = dataInizio;
        while (!curr.isAfter(dataFine)) {
            totalePostazione += postazioneScelta.getTariffaGiornaliera(curr);
            curr = curr.plusDays(1);
        }

        double totaleServizi = 0.0;
        for (ServizioPrenotato sp : serviziPrenotati) {
            totaleServizi += sp.calcolaSubTotale(dataInizio, dataFine);
        }

        this.importoTotale = totalePostazione + totaleServizi;
        return this.importoTotale;
    }

    public String getIdPrenotazione() { return idPrenotazione; }
    public void setIdPrenotazione(String idPrenotazione) { this.idPrenotazione = idPrenotazione; }
    public LocalDate getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(LocalDate dataCreazione) { this.dataCreazione = dataCreazione; }
    public LocalDate getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio = dataInizio; }
    public LocalDate getDataFine() { return dataFine; }
    public void setDataFine(LocalDate dataFine) { this.dataFine = dataFine; }
    public String getOraInizio() { return oraInizio; }
    public void setOraInizio(String oraInizio) { this.oraInizio = oraInizio; }
    public StatoPrenotazione getStato() { return stato; }
    public void setStato(StatoPrenotazione stato) { this.stato = stato; }
    public double getImportoTotale() { return importoTotale; }
    public void setImportoTotale(double importoTotale) { this.importoTotale = importoTotale; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Postazione getPostazioneScelta() { return postazioneScelta; }
    public void setPostazioneScelta(Postazione postazioneScelta) { this.postazioneScelta = postazioneScelta; }
    public List<ServizioPrenotato> getServiziPrenotati() { return serviziPrenotati; }
    public void setServiziPrenotati(List<ServizioPrenotato> serviziPrenotati) { this.serviziPrenotati = serviziPrenotati; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prenotazione that = (Prenotazione) o;
        return Objects.equals(idPrenotazione, that.idPrenotazione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrenotazione);
    }
}