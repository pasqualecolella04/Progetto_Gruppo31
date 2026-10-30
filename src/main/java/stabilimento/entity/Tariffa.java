package stabilimento.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Tariffa {
    private String idTariffa;
    private String stagione;
    private double prezzoGiornaliero;
    private LocalDate dataInizioValidita;
    private LocalDate dataFineValidita;

    public Tariffa(String idTariffa, String stagione, double prezzoGiornaliero, LocalDate dataInizioValidita, LocalDate dataFineValidita) {
        this.idTariffa = idTariffa;
        this.stagione = stagione;
        this.prezzoGiornaliero = prezzoGiornaliero;
        this.dataInizioValidita = dataInizioValidita;
        this.dataFineValidita = dataFineValidita;
    }

    public boolean isValidaInData(LocalDate data) {
        if (data == null || dataInizioValidita == null || dataFineValidita == null) {
            return false;
        }
        return !data.isBefore(dataInizioValidita) && !data.isAfter(dataFineValidita);
    }

    public String getIdTariffa() { return idTariffa; }
    public void setIdTariffa(String idTariffa) { this.idTariffa = idTariffa; }
    public String getStagione() { return stagione; }
    public void setStagione(String stagione) { this.stagione = stagione; }
    public double getPrezzoGiornaliero() { return prezzoGiornaliero; }
    public void setPrezzoGiornaliero(double prezzoGiornaliero) { this.prezzoGiornaliero = prezzoGiornaliero; }
    public LocalDate getDataInizioValidita() { return dataInizioValidita; }
    public void setDataInizioValidita(LocalDate dataInizioValidita) { this.dataInizioValidita = dataInizioValidita; }
    public LocalDate getDataFineValidita() { return dataFineValidita; }
    public void setDataFineValidita(LocalDate dataFineValidita) { this.dataFineValidita = dataFineValidita; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tariffa tariffa = (Tariffa) o;
        return Objects.equals(idTariffa, tariffa.idTariffa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTariffa);
    }
}
