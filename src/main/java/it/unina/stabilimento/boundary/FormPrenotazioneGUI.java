package it.unina.stabilimento.boundary;

import it.unina.stabilimento.controller.PrenotazioneController;
import it.unina.stabilimento.dto.PrenotazioneDTO;
import it.unina.stabilimento.entity.Postazione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormPrenotazioneGUI extends JFrame {
    private JPanel contentPane;
    private JTextField txtIdCliente;
    private JTextField txtDataInizio;
    private JTextField txtDataFine;
    private JComboBox<Postazione> cmbPostazioniDisponibili;
    private JTextField txtQtaLettini;
    private JTextField txtQtaCabina;
    private JButton btnCercaDisponibilita;
    private JButton btnCalcolaPreventivo;
    private JButton btnConfermaPrenotazione;
    private JLabel lblPreventivo;
    private JTable tblRiepilogoPrenotazioni;
    private DefaultTableModel tableModel;

    public FormPrenotazioneGUI() {
        setTitle("Stabilimento Balneare Gruppo 31 - UC1: Effettua Prenotazione");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);

        initComponents();
        initListeners();
    }

    private void initComponents() {
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Dati Richiesta Prenotazione"));

        txtIdCliente = new JTextField("C102");
        txtDataInizio = new JTextField("2026-08-01");
        txtDataFine = new JTextField("2026-08-05");
        cmbPostazioniDisponibili = new JComboBox<>();
        txtQtaLettini = new JTextField("0");
        txtQtaCabina = new JTextField("0");

        formPanel.add(new JLabel("ID Cliente Autenticato (*):"));
        formPanel.add(txtIdCliente);
        formPanel.add(new JLabel("Data Inizio (YYYY-MM-DD) (*):"));
        formPanel.add(txtDataInizio);
        formPanel.add(new JLabel("Data Fine (YYYY-MM-DD) (*):"));
        formPanel.add(txtDataFine);
        formPanel.add(new JLabel("Postazione Disponibile (*):"));
        formPanel.add(cmbPostazioniDisponibili);
        formPanel.add(new JLabel("Quantità Lettini Extra (ID: S01):"));
        formPanel.add(txtQtaLettini);
        formPanel.add(new JLabel("Quantità Cabina Privata (ID: S02):"));
        formPanel.add(txtQtaCabina);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnCercaDisponibilita = new JButton("1. Cerca Postazioni Libere");
        btnCalcolaPreventivo = new JButton("2. Calcola Preventivo");
        btnConfermaPrenotazione = new JButton("3. Conferma Prenotazione");
        lblPreventivo = new JLabel("Preventivo: € 0.00");
        lblPreventivo.setFont(new Font("SansSerif", Font.BOLD, 14));

        actionPanel.add(btnCercaDisponibilita);
        actionPanel.add(btnCalcolaPreventivo);
        actionPanel.add(btnConfermaPrenotazione);
        actionPanel.add(lblPreventivo);

        JPanel northContainer = new JPanel(new BorderLayout(5, 5));
        northContainer.add(formPanel, BorderLayout.CENTER);
        northContainer.add(actionPanel, BorderLayout.SOUTH);

        String[] columns = {"ID Prenotazione", "Cliente", "Postazione", "Dal", "Al", "Giorni", "Servizi Extra", "Totale (€)", "Stato"};
        tableModel = new DefaultTableModel(columns, 0);
        tblRiepilogoPrenotazioni = new JTable(tableModel);

        contentPane.add(northContainer, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(tblRiepilogoPrenotazioni), BorderLayout.CENTER);
        setContentPane(contentPane);
    }

    private void initListeners() {
        // Listener 1: Ricerca Postazioni Disponibili
        btnCercaDisponibilita.addActionListener(e -> {
            try {
                LocalDate inizio = validaSintassiData(txtDataInizio.getText(), "Data Inizio");
                LocalDate fine = validaSintassiData(txtDataFine.getText(), "Data Fine");

                List<Postazione> libere = PrenotazioneController.getInstance()
                        .visualizzaPostazioniDisponibili(inizio, fine);

                cmbPostazioniDisponibili.removeAllItems();
                if (libere.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Nessuna postazione disponibile per le date selezionate.",
                            "Disponibilità Esaurita", JOptionPane.WARNING_MESSAGE);
                } else {
                    for (Postazione p : libere) {
                        cmbPostazioniDisponibili.addItem(p);
                    }
                    JOptionPane.showMessageDialog(this,
                            "Trovate " + libere.size() + " postazioni disponibili.",
                            "Ricerca Completata", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore di Validazione", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Listener 2: Calcolo Preventivo
        btnCalcolaPreventivo.addActionListener(e -> {
            try {
                LocalDate inizio = validaSintassiData(txtDataInizio.getText(), "Data Inizio");
                LocalDate fine = validaSintassiData(txtDataFine.getText(), "Data Fine");
                Postazione selezionata = (Postazione) cmbPostazioniDisponibili.getSelectedItem();
                if (selezionata == null) {
                    throw new IllegalArgumentException("Selezionare prima una postazione dalla lista.");
                }
                Map<String, Integer> mappaServizi = estraiServiziSintatticamenteValidi();

                double preventivo = PrenotazioneController.getInstance()
                        .calcolaPreventivoPrenotazione(selezionata.getIdPostazione(), mappaServizi, inizio, fine);
                lblPreventivo.setText(String.format("Preventivo: € %.2f", preventivo));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore Calcolo Preventivo", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Listener 3: Conferma e Registrazione Prenotazione
        btnConfermaPrenotazione.addActionListener(e -> {
            try {
                String idCliente = txtIdCliente.getText().trim();
                if (idCliente.isEmpty()) {
                    throw new IllegalArgumentException("Il campo ID Cliente è obbligatorio.");
                }
                LocalDate inizio = validaSintassiData(txtDataInizio.getText(), "Data Inizio");
                LocalDate fine = validaSintassiData(txtDataFine.getText(), "Data Fine");
                Postazione selezionata = (Postazione) cmbPostazioniDisponibili.getSelectedItem();
                if (selezionata == null) {
                    throw new IllegalArgumentException("Selezionare una postazione disponibile.");
                }
                Map<String, Integer> mappaServizi = estraiServiziSintatticamenteValidi();

                PrenotazioneDTO dto = PrenotazioneController.getInstance()
                        .effettuaPrenotazione(idCliente, selezionata.getIdPostazione(), mappaServizi, inizio, fine);

                tableModel.addRow(new Object[]{
                        dto.getIdPrenotazione(),
                        dto.getNominativoCliente(),
                        dto.getIdPostazione(),
                        dto.getDataInizio(),
                        dto.getDataFine(),
                        dto.getGiorniPermanenza(),
                        dto.getNumeroServiziAccessori(),
                        String.format("%.2f", dto.getImportoTotale()),
                        dto.getStato()
                });

                lblPreventivo.setText(String.format("Totale Confermato: € %.2f", dto.getImportoTotale()));
                JOptionPane.showMessageDialog(this,
                        "Prenotazione " + dto.getIdPrenotazione() + " registrata con successo nello stato: " + dto.getStato(),
                        "Conferma Prenotazione", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore Prenotazione", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private LocalDate validaSintassiData(String input, String nomeCampo) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' non può essere vuoto.");
        }
        try {
            return LocalDate.parse(input.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Formato data non valido per '" + nomeCampo + "'. Usa YYYY-MM-DD.");
        }
    }

    private Map<String, Integer> estraiServiziSintatticamenteValidi() {
        try {
            int qtaLettini = Integer.parseInt(txtQtaLettini.getText().trim());
            int qtaCabina = Integer.parseInt(txtQtaCabina.getText().trim());
            if (qtaLettini < 0 || qtaCabina < 0) {
                throw new IllegalArgumentException("Le quantità dei servizi aggiuntivi non possono essere negative.");
            }
            Map<String, Integer> map = new HashMap<>();
            if (qtaLettini > 0) map.put("S01", qtaLettini);
            if (qtaCabina > 0) map.put("S02", qtaCabina);
            return map;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Inserire valori numerici interi validi per le quantità dei servizi.");
        }
    }
}