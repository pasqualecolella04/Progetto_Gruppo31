package stabilimento.controller;

import stabilimento.database.PostazioneDAO;
import stabilimento.database.PrenotazioneDAO;
import stabilimento.database.ServizioAggiuntivoDAO;
import stabilimento.database.UtenteDAO;
import stabilimento.dto.PrenotazioneDTO;
import stabilimento.entity.*;
import stabilimento.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PrenotazioneController {
    private static PrenotazioneController instance;

    private final PostazioneDAO postazioneDAO;
    private final PrenotazioneDAO prenotazioneDAO;
    private final UtenteDAO utenteDAO;
    private final ServizioAggiuntivoDAO servizioDAO;

    private PrenotazioneController() {
        this.postazioneDAO = new PostazioneDAO();
        this.prenotazioneDAO = new PrenotazioneDAO();
        this.utenteDAO = new UtenteDAO();
        this.servizioDAO = new ServizioAggiuntivoDAO();
    }

    public static synchronized PrenotazioneController getInstance() {
        if (instance == null) {
            instance = new PrenotazioneController();
        }
        return instance;
    }

    public List<Postazione> visualizzaPostazioniDisponibili(LocalDate dataInizio, LocalDate dataFine) {
        validaIntervalloDate(dataInizio, dataFine);
        return postazioneDAO.findDisponibili(dataInizio, dataFine);
    }

    public double calcolaPreventivoPrenotazione(String idPostazione, Map<String, Integer> serviziRichiesti,
                                                LocalDate dataInizio, LocalDate dataFine) {
        validaIntervalloDate(dataInizio, dataFine);
        Postazione postazione = postazioneDAO.findByID(idPostazione);
        if (postazione == null) {
            throw new IllegalArgumentException("Postazione non trovata: " + idPostazione);
        }

        Cliente dummyCliente = new Cliente("TEMP", "Temp", "Temp", "temp@unina.it", "000", "pwd", "CF");
        Prenotazione temp = new Prenotazione("PREV-TEMP", dataInizio, dataFine, dummyCliente, postazione);

        if (serviziRichiesti != null) {
            for (Map.Entry<String, Integer> entry : serviziRichiesti.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    ServizioAggiuntivo sa = servizioDAO.findByID(entry.getKey());
                    if (sa == null) {
                        throw new IllegalArgumentException("Servizio aggiuntivo inesistente: " + entry.getKey());
                    }
                    temp.aggiungiServizioPrenotato(sa, entry.getValue());
                }
            }
        }
        return temp.calcolaImportoTotale();
    }

    public PrenotazioneDTO effettuaPrenotazione(String idCliente, String idPostazione,
                                                Map<String, Integer> serviziRichiesti,
                                                LocalDate dataInizio, LocalDate dataFine) {
        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Identificativo cliente obbligatorio.");
        }
        if (idPostazione == null || idPostazione.isBlank()) {
            throw new IllegalArgumentException("Identificativo postazione obbligatorio.");
        }
        validaIntervalloDate(dataInizio, dataFine);

        Cliente cliente = utenteDAO.findClienteById(idCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente non registrato a sistema: " + idCliente);
        }

        Postazione postazione = postazioneDAO.findByID(idPostazione);
        if (postazione == null) {
            throw new IllegalArgumentException("Postazione selezionata inesistente: " + idPostazione);
        }

        // Verifica concorrenza / occupazione simultanea nel periodo (Step 5.4 UC1)
        if (prenotazioneDAO.checkConflittoPostazione(idPostazione, dataInizio, dataFine)) {
            throw new IllegalStateException("La postazione " + idPostazione + " risulta già occupata nel periodo selezionato.");
        }

        String idPrenotazione = "PREN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Prenotazione nuovaPrenotazione = new Prenotazione(idPrenotazione, dataInizio, dataFine, cliente, postazione);

        if (serviziRichiesti != null) {
            for (Map.Entry<String, Integer> entry : serviziRichiesti.entrySet()) {
                int qta = entry.getValue() != null ? entry.getValue() : 0;
                if (qta > 0) {
                    ServizioAggiuntivo servizio = servizioDAO.findByID(entry.getKey());
                    if (servizio == null) {
                        throw new IllegalArgumentException("Servizio aggiuntivo non trovato: " + entry.getKey());
                    }
                    nuovaPrenotazione.aggiungiServizioPrenotato(servizio, qta);
                }
            }
        }

        nuovaPrenotazione.calcolaImportoTotale();
        prenotazioneDAO.save(nuovaPrenotazione);
        cliente.aggiungiPrenotazioneStorico(nuovaPrenotazione);

        return toDTO(nuovaPrenotazione);
    }

    private void validaIntervalloDate(LocalDate dataInizio, LocalDate dataFine) {
        if (dataInizio == null || dataFine == null) {
            throw new IllegalArgumentException("Le date di inizio e fine soggiorno sono obbligatorie.");
        }
        if (dataInizio.isAfter(dataFine)) {
            throw new IllegalArgumentException("La data di inizio non può essere successiva alla data di fine.");
        }
    }

    public PrenotazioneDTO toDTO(Prenotazione p) {
        int totServizi = p.getServiziPrenotati().stream()
                .mapToInt(ServizioPrenotato::getQuantitaRichiesta)
                .sum();
        return new PrenotazioneDTO(
                p.getIdPrenotazione(),
                p.getCliente().getIdUtente(),
                p.getCliente().getNome() + " " + p.getCliente().getCognome(),
                p.getPostazioneScelta().getIdPostazione(),
                p.getPostazioneScelta().toString(),
                p.getDataInizio(),
                p.getDataFine(),
                p.calcolaGiorniPermanenza(),
                p.getStato().name(),
                p.getImportoTotale(),
                totServizi
        );
    }
}