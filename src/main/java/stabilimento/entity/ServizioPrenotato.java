package stabilimento.entity;

import java.time.LocalDate;
import java.util.Objects;

public class ServizioPrenotato {
    private ServizioAggiuntivo servizioRiferimento;
    private int quantitaRichiesta;
    private double subTotale;

    public ServizioPrenotato(ServizioAggiuntivo servizioRiferimento, int quantitaRichiesta) {
        this.servizioRiferimento = servizioRiferimento;
        this.quantitaRichiesta = quantitaRichiesta;
        this.subTotale = 0.0;
    }

    public double calcolaSubTotale(LocalDate dataInizio, LocalDate dataFine) {
        double totale = 0.0;
        LocalDate curr = dataInizio;
        while (!curr.isAfter(dataFine)) {
            totale += servizioRiferimento.getPrezzoGiornaliero(curr) * quantitaRichiesta;
            curr = curr.plusDays(1);
        }
        this.subTotale = totale;
        return this.subTotale;
    }

    public ServizioAggiuntivo getServizioRiferimento() { return servizioRiferimento; }
    public void setServizioRiferimento(ServizioAggiuntivo servizioRiferimento) { this.servizioRiferimento = servizioRiferimento; }
    public int getQuantitaRichiesta() { return quantitaRichiesta; }
    public void setQuantitaRichiesta(int quantitaRichiesta) { this.quantitaRichiesta = quantitaRichiesta; }
    public double getSubTotale() { return subTotale; }
    public void setSubTotale(double subTotale) { this.subTotale = subTotale; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServizioPrenotato that = (ServizioPrenotato) o;
        return Objects.equals(servizioRiferimento, that.servizioRiferimento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servizioRiferimento);
    }
}