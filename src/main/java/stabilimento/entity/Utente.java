package stabilimento.entity;

import java.util.Objects;

public abstract class Utente {
    protected String idUtente;
    protected String nome;
    protected String cognome;
    protected String email;
    protected String telefono;
    protected String passwordHash;
    protected String ruolo;

    public Utente(String idUtente, String nome, String cognome, String email, String telefono, String passwordHash, String ruolo) {
        this.idUtente = idUtente;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.ruolo = ruolo;
    }

    public boolean verificaCredenziali(String pwd) {
        return this.passwordHash != null && this.passwordHash.equals(pwd);
    }

    public String getIdUtente() { return idUtente; }
    public void setIdUtente(String idUtente) { this.idUtente = idUtente; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utente)) return false;
        Utente utente = (Utente) o;
        return Objects.equals(idUtente, utente.idUtente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUtente);
    }
}
