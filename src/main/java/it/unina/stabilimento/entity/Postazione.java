package it.unina.stabilimento.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Postazione {
    private String idPostazione;
    private int numeroProgressivo;
    private int fila;
    private TipoPostazione posizione;
    private boolean occupata;
    private List<Tariffa> listinoTariffe;

    public Postazione(String idPostazione, int numeroProgressivo, int fila, TipoPostazione posizione) {
        this.idPostazione = idPostazione;
        this.numeroProgressivo = numeroProgressivo;
        this.fila = fila;
        this.posizione = posizione;
        this.occupata = false;
        this.listinoTariffe = new ArrayList<>();
    }

    public void aggiungiTariffa(Tariffa t) {
        if (t != null && !this.listinoTariffe.contains(t)) {
            this.listinoTariffe.add(t);
        }
    }

    /**
     * Calcola la tariffa giornaliera applicabile per la data richiesta.
     * Se nessuna tariffa stagionale copre la data, applica il prezzo base per fila/posizione.
     */
    public double getTariffaGiornaliera(LocalDate data) {
        for (Tariffa t : listinoTariffe) {
            if (t.isValidaInData(data)) {
                return t.getPrezzoGiornaliero();
            }
        }
        return switch (posizione) {
            case VIP -> 40.0;
            case PRIMA_FILA -> 30.0;
            case SECONDA_FILA -> 25.0;
            case STANDARD -> 20.0;
        };
    }

    public String getIdPostazione() { return idPostazione; }
    public void setIdPostazione(String idPostazione) { this.idPostazione = idPostazione; }
    public int getNumeroProgressivo() { return numeroProgressivo; }
    public void setNumeroProgressivo(int numeroProgressivo) { this.numeroProgressivo = numeroProgressivo; }
    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }
    public TipoPostazione getPosizione() { return posizione; }
    public void setPosizione(TipoPostazione posizione) { this.posizione = posizione; }
    public boolean isOccupata() { return occupata; }
    public void setOccupata(boolean occupata) { this.occupata = occupata; }
    public List<Tariffa> getListinoTariffe() { return listinoTariffe; }
    public void setListinoTariffe(List<Tariffa> listinoTariffe) { this.listinoTariffe = listinoTariffe; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Postazione that = (Postazione) o;
        return Objects.equals(idPostazione, that.idPostazione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPostazione);
    }

    @Override
    public String toString() {
        return idPostazione + " - Fila " + fila + " (N. " + numeroProgressivo + " - " + posizione + ")";
    }
}