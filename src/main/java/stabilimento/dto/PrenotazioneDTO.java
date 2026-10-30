package stabilimento.dto;

import java.time.LocalDate;

public class PrenotazioneDTO {
    private final String idPrenotazione;
    private final String idCliente;
    private final String nominativoCliente;
    private final String idPostazione;
    private final String descrizionePostazione;
    private final LocalDate dataInizio;
    private final LocalDate dataFine;
    private final long giorniPermanenza;
    private final String stato;
    private final double importoTotale;
    private final int numeroServiziAccessori;

    public PrenotazioneDTO(String idPrenotazione, String idCliente, String nominativoCliente,
                           String idPostazione, String descrizionePostazione, LocalDate dataInizio,
                           LocalDate dataFine, long giorniPermanenza, String stato,
                           double importoTotale, int numeroServiziAccessori) {
        this.idPrenotazione = idPrenotazione;
        this.idCliente = idCliente;
        this.nominativoCliente = nominativoCliente;
        this.idPostazione = idPostazione;
        this.descrizionePostazione = descrizionePostazione;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.giorniPermanenza = giorniPermanenza;
        this.stato = stato;
        this.importoTotale = importoTotale;
        this.numeroServiziAccessori = numeroServiziAccessori;
    }

    public String getIdPrenotazione() { return idPrenotazione; }
    public String getIdCliente() { return idCliente; }
    public String getNominativoCliente() { return nominativoCliente; }
    public String getIdPostazione() { return idPostazione; }
    public String getDescrizionePostazione() { return descrizionePostazione; }
    public LocalDate getDataInizio() { return dataInizio; }
    public LocalDate getDataFine() { return dataFine; }
    public long getGiorniPermanenza() { return giorniPermanenza; }
    public String getStato() { return stato; }
    public double getImportoTotale() { return importoTotale; }
    public int getNumeroServiziAccessori() { return numeroServiziAccessori; }
}