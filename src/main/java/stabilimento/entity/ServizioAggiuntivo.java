package stabilimento.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServizioAggiuntivo {
    private String idServizio;
    private String nomeServizio;
    private String descrizione;
    private int disponibilitaMax;
    private double prezzoBaseGiornaliero;
    private List<Tariffa> tariffeServizio;

    public ServizioAggiuntivo(String idServizio, String nomeServizio, String descrizione, int disponibilitaMax, double prezzoBaseGiornaliero) {
        this.idServizio = idServizio;
        this.nomeServizio = nomeServizio;
        this.descrizione = descrizione;
        this.disponibilitaMax = disponibilitaMax;
        this.prezzoBaseGiornaliero = prezzoBaseGiornaliero;
        this.tariffeServizio = new ArrayList<>();
    }

    public boolean verificaDisponibilita(int quantitaRichiesta) {
        return quantitaRichiesta > 0 && quantitaRichiesta <= this.disponibilitaMax;
    }

    public double getPrezzoGiornaliero(LocalDate data) {
        for (Tariffa t : tariffeServizio) {
            if (t.isValidaInData(data)) {
                return t.getPrezzoGiornaliero();
            }
        }
        return prezzoBaseGiornaliero;
    }

    public String getIdServizio() { return idServizio; }
    public void setIdServizio(String idServizio) { this.idServizio = idServizio; }
    public String getNomeServizio() { return nomeServizio; }
    public void setNomeServizio(String nomeServizio) { this.nomeServizio = nomeServizio; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public int getDisponibilitaMax() { return disponibilitaMax; }
    public void setDisponibilitaMax(int disponibilitaMax) { this.disponibilitaMax = disponibilitaMax; }
    public double getPrezzoBaseGiornaliero() { return prezzoBaseGiornaliero; }
    public void setPrezzoBaseGiornaliero(double prezzoBaseGiornaliero) { this.prezzoBaseGiornaliero = prezzoBaseGiornaliero; }
    public List<Tariffa> getTariffeServizio() { return tariffeServizio; }
    public void setTariffeServizio(List<Tariffa> tariffeServizio) { this.tariffeServizio = tariffeServizio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServizioAggiuntivo that = (ServizioAggiuntivo) o;
        return Objects.equals(idServizio, that.idServizio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idServizio);
    }
}