import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Παναγιώτης on 22/11/2017.
 */
public class Simulator {

    public static SimulationFrame frame;
    public static boolean running = false;
    public static double simulatedTime = 0;

    public static  AtomicInteger totalAircrafts = new AtomicInteger(0); // multiple threads trying to access those data
    public static AtomicInteger collisions = new AtomicInteger(0);
    public static AtomicInteger landings = new AtomicInteger(0);

    public static int numberOfFlights;
    public static  Flights[] availableFlights = new Flights[100]; //100 max flights
    public static int[][] flightsPosition = new int[3][100]; //100 max flights
    public static String[] whyStopped = new String[100]; //100 max flights
    public static Thread[] currentFlights = new Thread[100]; //100 max flights

    public static ArbitratorServer myServer;
    public static SimulatorTimer myTimer;

    public static JScrollPane scrollPaneFlights;
    public static JScrollPane scrollPaneAircrafts;
    public static JTextArea popUpTextAreaFlights;
    public static JTextArea popUpTextAreaAircrafts;

    public static void main(String[] args) throws Exception {
        SimulationFrame myFrame = new SimulationFrame(); // initialize frame
        frame = myFrame;

        // Fill simulation parameters...
        SingleMotor.sleepTimeInPhase[1] = SingleMotor.sleepTimeInPhase[3] = 6250;  // time for take off-on
        Turboprop.sleepTimeInPhase[1] = Turboprop.sleepTimeInPhase[3] = 3750;
        Jet.sleepTimeInPhase[1] = Jet.sleepTimeInPhase[3] = 2678;

        SingleMotor.sleepTimeInPhase[4] = 7; // up-down
        Turboprop.sleepTimeInPhase[4] = 4;
        Jet.sleepTimeInPhase[4] = 2;

        SingleMotor.sleepTimeInPhase[5] = 0; // finished so wait no more
        Turboprop.sleepTimeInPhase[5] = 0;
        Jet.sleepTimeInPhase[5] = 0;

        SingleMotor.fuelPerPixel = 3.75d; // fuel per pixel
        Turboprop.fuelPerPixel = 11.25d;
        Jet.fuelPerPixel = 18.75d;
    }


    public static void createFlights(String MAPID) {
        // when load button is pressed, then we should initialize the flights and save them
        String lineOfNumbers;
        String[] line = new String[9];
        int id, t, srcAirport, destAirport, velocity, height, fuel;
        byte type;
        String name;
        String valid;
        try {
            Scanner f = new Scanner(new FileReader("flights_" + MAPID + ".txt"));
            int i = 0;
            while (f.hasNextLine()) {
                lineOfNumbers = f.nextLine();
                line = lineOfNumbers.split(",");
                if (line.length < 9) break;
                id = Integer.parseInt(line[0]);
                t = Integer.parseInt(line[1]);
                destAirport = Integer.parseInt(line[2]);
                srcAirport = Integer.parseInt(line[3]);
                name = line[4];
                type = (byte) Integer.parseInt(line[5]);
                velocity = Integer.parseInt(line[6]);
                height = Integer.parseInt(line[7]);
                fuel = Integer.parseInt(line[8]);

                availableFlights[i] = new Flights(id, t, srcAirport, destAirport, name, type, velocity, height, fuel, i);
                valid = availableFlights[i].isItValid();
                if (!valid.equals("")) {
                    availableFlights[i].flightIsValid = false;
                    frame.myMessagePanel.addNewMessage("Flight with id " + id + " and name " + name + " is not valid: " + valid, MessagePanel.WARNING);
                }
                i++;
            }
            numberOfFlights = i;
            f.close();
        } catch (FileNotFoundException ex) {
            if (!MAPID.equals("")) {
                System.out.println("cannot find flights_" + MAPID);
                JOptionPane.showMessageDialog(frame, "cannot find world or airport or flights with the ending _" + MAPID);
            }
        }
    }


    static void arbitrate() {
        myServer = new ArbitratorServer(); //start a server to listen to a socket and communicate with flights
        myServer.start();

        myTimer = new SimulatorTimer(); // start the global timer
        myTimer.start();
    }


    public static void startFlights() {
        // when start button is pressed, jmenubar call this method
        running = true;
        for (int i = 0; i < numberOfFlights; i++) {
            flightsPosition[0][i] = 1000;
            flightsPosition[1][i] = 1000; // means that flight has not started yet
        }
        for (int i = 0; i < numberOfFlights; i++) {
            whyStopped[i] = "";
        }

        for (int i = 0; i < numberOfFlights; i++) {
            if (availableFlights[i].flightIsValid) {
                (currentFlights[i] = new Thread(availableFlights[i])).start(); // for each flight we start a new thread
            }
        }
        arbitrate(); // now, listen to flights

    }

    public static void stopFlights() {
        // when stop button is pressed, jmenubar call this method
        // close all threads, timers and sockets that were opened
        for (int i = 0; i < numberOfFlights; i++) {
            if (availableFlights[i].flightIsValid) {
                whyStopped[i] = "forced"; // explain to the flight that it will be stopped because of user
                currentFlights[i].interrupt();
                try {
                    currentFlights[i].join();
                } catch (Exception ex) {
                    System.out.println("Problem joining with thread: " + i);
                }
            }
        }
        synchronized (myTimer.alarm) {
            myTimer.alarm.notify();
        }
        try {
            myServer.getServerSocket().close();
            myServer.join();
        } catch (Exception ex) {
            System.out.println("Problem joining with myserver because of");
            System.out.println();
        }
        landings = new AtomicInteger(0);
        collisions = new AtomicInteger(0);
        Simulator.frame.myInfoBar.myRefresh();
        running = false;
    }


    static void showAirports() {
        // when airports button is pressed, jmenubar call this method
        String myMesg = "Airports on this Simulation: \n";
        for (int j = 0; j < frame.myMap.numberOfairports; j++) {
            Airports curAirport = frame.myMap.getAirportAt(j);
            myMesg = myMesg + String.format("\n Airport with: \n id: %s \n name: %s \n orientation: %s \n type: %s \n open: %s \n", curAirport.getId(), curAirport.getAirportName(), curAirport.getStringOrientation(), curAirport.getType(), curAirport.getOpen());
        }
        JTextArea help = new JTextArea(myMesg);
        scrollPaneAircrafts = new JScrollPane(help,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneAircrafts.setPreferredSize(new Dimension(400, 480));
        JOptionPane.showMessageDialog(frame, scrollPaneAircrafts);
    }


    private static int getVelocityOfFlight(Flights curFlight) {
        // velocity depends on phase and type of plane
        if (curFlight.typeOfPlane == 1) {
            if (curFlight.getPhase() == 1 || curFlight.getPhase() == 3) {
                return SingleMotor.takeOffOnVelocity;
            } else if (curFlight.getPhase() == 2) {
                return curFlight.fVelocity;
            }
        } else if (curFlight.typeOfPlane == 2) {
            if (curFlight.getPhase() == 1 || curFlight.getPhase() == 3) {
                return Turboprop.takeOffOnVelocity;
            } else if (curFlight.getPhase() == 2) {
                return curFlight.fVelocity;
            }
        } else if (curFlight.typeOfPlane == 3) {
            if (curFlight.getPhase() == 1 || curFlight.getPhase() == 3) {
                return Jet.takeOffOnVelocity;
            } else if (curFlight.getPhase() == 2) {
                return curFlight.fVelocity;
            }
        }
        return 0;
    }


    static String makeAircraftsMessage() {
        String myMesg = "Aircrafts on this Simulation: \n";
        for (int j = 0; j < numberOfFlights; j++) {
            String nameSrcAirport, nameDstAirport;
            int velocity, height;
            double fuel;
            // for every running flight...
            if (availableFlights[j].hasStarted) {
                nameSrcAirport = (frame.myMap.getAirportWithId(availableFlights[j].srcAirport).getAirportName());
                nameDstAirport = (frame.myMap.getAirportWithId(availableFlights[j].destAirport).getAirportName());
                velocity = getVelocityOfFlight(availableFlights[j]);
                height = availableFlights[j].planeClone.height;
                fuel = availableFlights[j].currentFuel;
                myMesg = myMesg + String.format("\n Aircraft with: \n name of source aiport: %s \n name of destination airport: %s \n velocity: %d knots\n height: %d feet \n fuel: %f kg \n", nameSrcAirport, nameDstAirport, velocity, height, fuel);

            }
        }
        return myMesg;
    }

    static void showAircrafts() {
        // when aircrafts button is pressed, jmenubar call this method
        String myMesg = makeAircraftsMessage();
        popUpTextAreaAircrafts = new JTextArea(myMesg);
        scrollPaneAircrafts = new JScrollPane(popUpTextAreaAircrafts,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneAircrafts.setPreferredSize(new Dimension(400, 480));
        // refresh panel every now and then, to get fresh values
        Timer refreshTimer = new Timer();
        TimerTask refreshTask = new TimerTask() {
            public void run() {
                String myMesg =  makeAircraftsMessage();
                popUpTextAreaAircrafts.setText(myMesg);
                frame.getContentPane().revalidate();
                frame.getContentPane().repaint();
            }
        };
        refreshTimer.schedule(refreshTask, 500, 1000); //sleeping time to start in ms
        JOptionPane.showMessageDialog(frame, scrollPaneAircrafts);
    }

    static String makeFlightsMessage() {
        String myMesg = "Flights on this Simulation: \n";
        for (int j = 0; j < numberOfFlights; j++) {
            if (availableFlights[j].flightIsValid) {
                String nameSrcAirport, nameDstAirport;
                int typeOfPlane, stateOfFlight;
                String state;
                nameSrcAirport = (frame.myMap.getAirportWithId(availableFlights[j].srcAirport).getAirportName());
                nameDstAirport = (frame.myMap.getAirportWithId(availableFlights[j].destAirport).getAirportName());
                typeOfPlane = availableFlights[j].typeOfPlane;
                state = availableFlights[j].state;
                myMesg = myMesg + String.format("\n Flights with: \n name of source aiport: %s \n name of destination airport: %s \n type of plane: %d \n Is running: %s \n ", nameSrcAirport, nameDstAirport, typeOfPlane, state);
            }
        }
        return myMesg;
    }

    static void showFlights() {
        // when flights button is pressed, jmenubar call this method
        String myMesg = makeFlightsMessage();
        popUpTextAreaFlights = new JTextArea(myMesg);
        scrollPaneFlights = new JScrollPane(popUpTextAreaFlights,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneFlights.setPreferredSize(new Dimension(400, 480));
        // refresh message every now and then in  order to show fresh values
        Timer refreshTimer = new Timer();
        TimerTask refreshTask = new TimerTask() {
            public void run() {
                String myMesg = makeFlightsMessage();
                popUpTextAreaFlights.setText(myMesg);
                frame.getContentPane().revalidate();
                frame.getContentPane().repaint();
            }
        };
        refreshTimer.schedule(refreshTask, 500, 1000); //sleeping time to start in ms
        JOptionPane.showMessageDialog(frame, scrollPaneFlights);
    }


}
