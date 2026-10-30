
    package it.unina.stabilimento;

import it.unina.stabilimento.boundary.FormPrenotazioneGUI;
import it.unina.stabilimento.boundary.FormReportGUI;
import it.unina.stabilimento.database.DBManager;
import it.unina.stabilimento.database.PostazioneDAO;
import it.unina.stabilimento.database.ServizioAggiuntivoDAO;
import it.unina.stabilimento.database.UtenteDAO;
import it.unina.stabilimento.entity.*;

import javax.swing.*;
import java.time.LocalDate;

    public class Main {
        public static void main(String[] args) {
            // 1. Inizializza lo schema e carica i dati minimi dimostrativi
            DBManager.getInstance().inizializzaSchemaDatabase();
            popolaDatiInizialiDemo();

            // 2. Avvia le interfacce grafiche nello Swing Event Dispatch Thread (EDT)
            SwingUtilities.invokeLater(() -> {
                FormPrenotazioneGUI guiCliente = new FormPrenotazioneGUI();
                guiCliente.setVisible(true);

                FormReportGUI guiGestore = new FormReportGUI();
                guiGestore.setLocation(guiCliente.getX() + 60, guiCliente.getY() + 60);
                guiGestore.setVisible(true);
            });
        }

        private static void popolaDatiInizialiDemo() {
            UtenteDAO utenteDAO = new UtenteDAO();
            if (utenteDAO.findClienteById("C102") == null) {
                utenteDAO.saveCliente(new Cliente("C102", "Mario", "Rossi", "mario.rossi@unina.it", "3331234567", "pwd123", "RSSMRA80A01F839X"));
            }

            PostazioneDAO postazioneDAO = new PostazioneDAO();
            if (postazioneDAO.findByID("POS-01") == null) {
                Postazione p1 = new Postazione("POS-01", 1, 1, TipoPostazione.PRIMA_FILA);
                p1.aggiungiTariffa(new Tariffa("TAR-01", "Alta Stagione", 35.0, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 9, 30)));
                postazioneDAO.save(p1);

                Postazione p2 = new Postazione("POS-02", 2, 1, TipoPostazione.PRIMA_FILA);
                postazioneDAO.save(p2);

                Postazione p3 = new Postazione("POS-03", 3, 2, TipoPostazione.STANDARD);
                postazioneDAO.save(p3);
            }

            ServizioAggiuntivoDAO servDAO = new ServizioAggiuntivoDAO();
            if (servDAO.findByID("S01") == null) {
                servDAO.save(new ServizioAggiuntivo("S01", "Lettino Extra", "Lettino pieghevole", 5, 8.0));
                servDAO.save(new ServizioAggiuntivo("S02", "Cabina Privata", "Cabina con doccia", 2, 15.0));
            }
        }
    }

