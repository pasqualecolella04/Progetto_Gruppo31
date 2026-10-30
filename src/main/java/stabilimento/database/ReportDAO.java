package stabilimento.database;

import stabilimento.entity.Prenotazione;
import java.time.LocalDate;
import java.util.List;

public class ReportDAO {
    private final PrenotazioneDAO prenotazioneDAO = new PrenotazioneDAO();

    public List<Prenotazione> estraiPrenotazioniPerPeriodo(LocalDate inizio, LocalDate fine) {
        return prenotazioneDAO.findByPeriodo(inizio, fine);
    }

    public List<Prenotazione> estraiPrenotazioniPerCliente(String idCliente) {
        return prenotazioneDAO.findByCliente(idCliente);
    }
}