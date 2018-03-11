import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static javax.swing.BorderFactory.createBevelBorder;
import static javax.swing.BorderFactory.createEmptyBorder;

/**
 * Created by Παναγιώτης on 25/11/2017.
 */
public class ΜyJMenuBar extends JMenuBar {


    public ΜyJMenuBar() {
        JMenu game = new JMenu("Game");
        this.add(game);
        JMenuItem start = new JMenuItem("Start");
        game.add(start);
        start.addActionListener(new startSimulation());
        JMenuItem stop = new JMenuItem("Stop");
        game.add(stop);
        stop.addActionListener(new stopSimulation());
        JMenuItem load = new JMenuItem("Load");
        game.add(load);
        load.addActionListener(new loadMap());
        JMenuItem exit = new JMenuItem("Exit");
        game.add(exit);
        exit.addActionListener(new exitaction());

        JMenu simulation = new JMenu("Simulation");
        this.add(simulation);
        JMenuItem airports = new JMenuItem("Airports");
        simulation.add(airports);
        airports.addActionListener(new airportsButtonListener());
        JMenuItem aircrafts = new JMenuItem("Aircrafts");
        simulation.add(aircrafts);
        aircrafts.addActionListener(new aircraftsButtonListener());
        JMenuItem flights = new JMenuItem("Flights");
        simulation.add(flights);
        flights.addActionListener(new flightsButtonListener());

        JMenu help = new JMenu("Help");
        this.add(help);
        JMenuItem about = new JMenuItem("About");
        help.add(about);
    }


    class exitaction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class airportsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (Simulator.running) {
                Simulator.showAirports();
            } else {
                JOptionPane.showMessageDialog(Simulator.frame, "Try starting a simulation first");
            }
        }
    }


    class aircraftsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (Simulator.running) {
                Simulator.showAircrafts();
            } else {
                JOptionPane.showMessageDialog(Simulator.frame, "Try starting a simulation first");
            }
        }
    }

    class flightsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (Simulator.running) {
                Simulator.showFlights();
            } else {
                JOptionPane.showMessageDialog(Simulator.frame, "Try starting a simulation first");
            }
        }
    }


    class loadMap implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (Simulator.running) {
                JOptionPane.showMessageDialog(Simulator.frame, "A current simulation is running, try stopping it first.");
            } else {
                String MAPID = JOptionPane.showInputDialog(null, "Give us your MapId", "Load Map", JOptionPane.QUESTION_MESSAGE);
                if (!MAPID.equals("")) {
                    Simulator.frame.reInit();
                    Simulator.frame.createMapAndMessages(MAPID);
                    Simulator.createFlights(MAPID);

                }
            }
        }
    }

    class startSimulation implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!Simulator.running) {
                Simulator.startFlights();
            } else {
                JOptionPane.showMessageDialog(Simulator.frame, "A current simulation is running, try stopping it first.");
            }
        }
    }


    class stopSimulation implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // call a simulator method to stop all flights
            Simulator.stopFlights();
            // clear my message panel
            Simulator.frame.myMessagePanel.clearMessagePanel();
        }
    }


}
