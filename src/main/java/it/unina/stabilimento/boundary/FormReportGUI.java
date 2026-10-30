package it.unina.stabilimento.boundary;

import it.unina.stabilimento.controller.ReportController;
import it.unina.stabilimento.dto.PrenotazioneDTO;
import it.unina.stabilimento.dto.ReportDTO;
import it.unina.stabilimento.entity.TipoReport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class FormReportGUI extends JFrame {
    private JPanel contentPane;
    private JComboBox<TipoReport> cmbTipoReport;
    private JTextField txtDataInizio;
    private JTextField txtDataFine;
    private JTextField txtIdCliente;
    private JComboBox<String> cmbFormatoEsportazione;
    private JButton btnGeneraAnteprima;
    private JButton btnEsportaFile;
    private JLabel lblSintesiReport;
    private JTable tblDettaglioReport;
    private DefaultTableModel tableModel;
    private ReportDTO ultimoReportGenerato;

    public FormReportGUI() {
        setTitle("Stabilimento Balneare Gruppo 31 - UC12: Genera Report (Area Gestore)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        initComponents();
        initListeners();
    }

    private void initComponents() {
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel filterPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtri di Selezione Reportistica"));

        cmbTipoReport = new JComboBox<>(TipoReport.values());
        txtDataInizio = new JTextField("2026-08-01");
        txtDataFine = new JTextField("2026-08-07");
        txtIdCliente = new JTextField("");
        cmbFormatoEsportazione = new JComboBox<>(new String[]{"CSV", "PDF"});

        filterPanel.add(new JLabel("Tipologia Report (*):"));
        filterPanel.add(cmbTipoReport);
        filterPanel.add(new JLabel("Data Inizio Periodo (YYYY-MM-DD):"));
        filterPanel.add(txtDataInizio);
        filterPanel.add(new JLabel("Data Fine Periodo (YYYY-MM-DD):"));
        filterPanel.add(txtDataFine);
        filterPanel.add(new JLabel("ID Cliente (obbligatorio se Tipo = CLIENTE):"));
        filterPanel.add(txtIdCliente);
        filterPanel.add(new JLabel("Formato di Esportazione:"));
        filterPanel.add(cmbFormatoEsportazione);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnGeneraAnteprima = new JButton("Genera Anteprima Report");
        btnEsportaFile = new JButton("Esporta File Report");
        btnEsportaFile.setEnabled(false);
        lblSintesiReport = new JLabel("Seleziona i parametri e premi 'Genera Anteprima Report'.");
        lblSintesiReport.setFont(new Font("SansSerif", Font.BOLD, 13));

        buttonPanel.add(btnGeneraAnteprima);
        buttonPanel.add(btnEsportaFile);
        buttonPanel.add(lblSintesiReport);

        JPanel topContainer = new JPanel(new BorderLayout(5, 5));
        topContainer.add(filterPanel, BorderLayout.CENTER);
        topContainer.add(buttonPanel, BorderLayout.SOUTH);

        String[] cols = {"ID Prenotazione", "Cliente", "Postazione", "Data Inizio", "Data Fine", "Stato", "Servizi Extra", "Importo (€)"};
        tableModel = new DefaultTableModel(cols, 0);
        tblDettaglioReport = new JTable(tableModel);

        contentPane.add(topContainer, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(tblDettaglioReport), BorderLayout.CENTER);
        setContentPane(contentPane);
    }

    private void initListeners() {
        btnGeneraAnteprima.addActionListener(e -> {
            try {
                TipoReport tipo = (TipoReport) cmbTipoReport.getSelectedItem();
                LocalDate inizio = null;
                LocalDate fine = null;
                String idCliente = txtIdCliente.getText().trim();

                if (tipo == TipoReport.SETTIMANALE || tipo == TipoReport.MENSILE) {
                    inizio = validaDataSintattica(txtDataInizio.getText(), "Data Inizio");
                    fine = validaDataSintattica(txtDataFine.getText(), "Data Fine");
                } else if (tipo == TipoReport.CLIENTE && idCliente.isEmpty()) {
                    throw new IllegalArgumentException("Inserire l'ID Cliente per generare un Report Cliente.");
                }

                ultimoReportGenerato = ReportController.getInstance().generaReport(tipo, inizio, fine, idCliente);

                tableModel.setRowCount(0);
                for (PrenotazioneDTO riga : ultimoReportGenerato.getDettaglioPrenotazioni()) {
                    tableModel.addRow(new Object[]{
                            riga.getIdPrenotazione(),
                            riga.getNominativoCliente(),
                            riga.getIdPostazione(),
                            riga.getDataInizio(),
                            riga.getDataFine(),
                            riga.getStato(),
                            riga.getNumeroServiziAccessori(),
                            String.format("%.2f", riga.getImportoTotale())
                    });
                }

                lblSintesiReport.setText(String.format(
                        "Report %s | Prenotazioni: %d | Incasso Totale: € %.2f | Media: € %.2f | Servizi Extra: %d",
                        ultimoReportGenerato.getIdReport(),
                        ultimoReportGenerato.getNumeroTotalePrenotazioni(),
                        ultimoReportGenerato.getIncassoTotale(),
                        ultimoReportGenerato.getIncassoMedioPerPrenotazione(),
                        ultimoReportGenerato.getTotaleServiziAccessoriVenduti()
                ));
                btnEsportaFile.setEnabled(true);

            } catch (Exception ex) {
                btnEsportaFile.setEnabled(false);
                tableModel.setRowCount(0);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Avviso Reportistica", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnEsportaFile.addActionListener(e -> {
            try {
                String formato = (String) cmbFormatoEsportazione.getSelectedItem();
                File fileEsportato = ReportController.getInstance().esportaReport(ultimoReportGenerato, formato);
                JOptionPane.showMessageDialog(this,
                        "Report esportato con successo in:\n" + fileEsportato.getAbsolutePath(),
                        "Esportazione Completata", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore Esportazione", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private LocalDate validaDataSintattica(String testo, String nomeCampo) {
        if (testo == null || testo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' è obbligatorio.");
        }
        try {
            return LocalDate.parse(testo.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Formato data non valido per '" + nomeCampo + "'. Usa YYYY-MM-DD.");
        }
    }
}