package it.unina.stabilimento.entity;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Utente {
    private String codiceFiscale;
    private List<Prenotazione> storicoPrenotazioni;

    public Cliente(String idUtente, String nome, String cognome, String email, String telefono, String passwordHash, String codiceFiscale) {
        super(idUtente, nome, cognome, email, telefono, passwordHash, "CLIENTE");
        this.codiceFiscale = codiceFiscale;
        this.storicoPrenotazioni = new ArrayList<>();
    }

    public void aggiungiPrenotazioneStorico(Prenotazione p) {
        if (p != null && !this.storicoPrenotazioni.contains(p)) {
            this.storicoPrenotazioni.add(p);
        }
    }

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }
    public List<Prenotazione> getStoricoPrenotazioni() { return storicoPrenotazioni; }
    public void setStoricoPrenotazioni(List<Prenotazione> storicoPrenotazioni) { this.storicoPrenotazioni = storicoPrenotazioni; }
}