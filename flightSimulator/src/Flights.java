import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Παναγιώτης on 29/11/2017.
 */
public class Flights extends Thread {
    public int threadId;
    public int flightId, startingTime, srcAirport, destAirport;
    public String name;
    public byte typeOfPlane;
    public int fVelocity, fHeight, fFuel; // velocity for phase 2, what height should i reach, with how much fuel should i start
    public long sleepTimeInPhase2, sleepTime;
    public double currentFuel;
    String heading = "";
    private boolean flightStarting = true; // I have not been started yet
    private boolean lastSquareReached = false; // I am not one square behind destination airport
    private boolean gotHeightNeeded = false; // I havent got height needed on top
    private boolean gotHeightNeeded2 = false; // I havent got height needed to land
    private boolean reachedOpenArea = false; // I am not ready to start phase 2
    public boolean hasStarted = false;
    public String state = "Pending"; // finished, failed, pending
    public boolean flightIsValid = true;
    private int phase;
    private Object mySync = new Object();
    Timer wakeUpTimer = new Timer();
    private Object waitTillStart = new Object();
    Timer startingTimer; // timer that wakes me when i should start
    Timer gainHeightTimer; // timer that increases my height over time
    Airplane planeClone; // it is the plane that is connected to the flight, it is airplane so it can be used for every type.
    int[] dests = new int[2]; // destination square (it's X and Y coordinate)


    public Flights(int flightId, int startingTime, int srcAirport, int destAirport, String name, byte typeOfPlane, int fVelocity, int fHeight, int fFuel, int threadId) {
        this.threadId = threadId;
        this.flightId = flightId;
        this.startingTime = startingTime;
        this.srcAirport = srcAirport;
        this.destAirport = destAirport;
        this.name = name;
        this.typeOfPlane = typeOfPlane;
        this.fVelocity = fVelocity;
        this.fHeight = fHeight;
        this.fFuel = fFuel;
        this.currentFuel = fFuel;
        //calculate sleeptimeinphase2
        sleepTimeInPhase2 = Math.round(60 * 60 * 1000 / (fVelocity * 0.8 * 12));
    }

    public String isItValid() {
        // Check if i am valid
        String isValid = "";
        if (!Simulator.frame.myMap.getAirportWithId(srcAirport).getOpen()) {
            isValid = "source airport is closed";
            return isValid;
        }
        if (!Simulator.frame.myMap.getAirportWithId(destAirport).getOpen()) {
            isValid = "destination airport is closed";
            return isValid;
        }
        if (this.typeOfPlane == 1) {
            if (this.fVelocity > SingleMotor.maxVelocity) isValid = "exceeds velocity";
            if (this.fHeight > SingleMotor.maxHeight) isValid = "exceeds height";
            if (this.fFuel > SingleMotor.maxFuel) isValid = "exceeds fuel";
            if (Simulator.frame.myMap.getAirportWithId(srcAirport).getType() == 2)
                isValid = "not valid source airport for single motor";
            if (Simulator.frame.myMap.getAirportWithId(destAirport).getType() == 2)
                isValid = "not valid destination airport for single motor";
        } else if (this.typeOfPlane == 2) {
            if (this.fVelocity > Turboprop.maxVelocity) isValid = "exceeds velocity";
            if (this.fHeight > Turboprop.maxHeight) isValid = "exceeds height";
            if (this.fFuel > Turboprop.maxFuel) isValid = "exceeds fuel";
            if (Simulator.frame.myMap.getAirportWithId(srcAirport).getType() == 1)
                isValid = "not valid source airport for turboprop";
            if (Simulator.frame.myMap.getAirportWithId(destAirport).getType() == 1)
                isValid = "not valid destination airport for turboprop";
        } else if (this.typeOfPlane == 3) {
            if (this.fVelocity > Jet.maxVelocity) isValid = "exceeds velocity";
            if (this.fHeight > Jet.maxHeight) isValid = "exceeds height";
            if (this.fFuel > Jet.maxFuel) isValid = "exceeds fuel";
            if (Simulator.frame.myMap.getAirportWithId(srcAirport).getType() == 1)
                isValid = "not valid source airport for jet";
            if (Simulator.frame.myMap.getAirportWithId(destAirport).getType() == 1)
                isValid = "not valid destination airport for jet";
        }
        return isValid;
    }

    private String validOrientation(byte numberForOrientation) {
        String orientation = "";
        if (numberForOrientation == 1) orientation = "n";
        if (numberForOrientation == 2) orientation = "e";
        if (numberForOrientation == 3) orientation = "s";
        if (numberForOrientation == 4) orientation = "w";
        return orientation;
    }

    private void flightSucceed(Airplane myPlane) {
        // Success
        gainHeightTimer.cancel();
        gainHeightTimer.purge();
        hasStarted = false; //finished so we dont want it in available flights
        state = "finished";
        myPlane.clearMyTxtArea();
        Simulator.flightsPosition[0][threadId] = 1000;
        Simulator.flightsPosition[1][threadId] = 1000; //finished dont count me
        Simulator.frame.mapFrame.remove(myPlane);
        Simulator.frame.myMessagePanel.addNewMessage("Flight with id " + this.flightId + " and name " + this.name + " has ended successfully ", MessagePanel.SUCCESS);
        Simulator.frame.getContentPane().revalidate();
        Simulator.frame.getContentPane().repaint();
        Simulator.totalAircrafts.decrementAndGet(); // total aircraft --
        Simulator.landings.incrementAndGet(); // landings ++
        Simulator.frame.myInfoBar.myRefresh();
    }


    private void flightEnded(Airplane myPlane) {
        // this flight came to an end
        String whyIstopped = "";
        //learn why i stopped
        whyIstopped = Simulator.whyStopped[threadId];
        while (Simulator.whyStopped[threadId].equals("")) {
            whyIstopped = Simulator.whyStopped[threadId];
        }
        String message = null;
        Color messageColor = null;
        if (whyIstopped.equals("forced")) {
            message = "Plane on flight with id " + this.flightId + " and name " + this.name + " has stopped by you ";
            messageColor = MessagePanel.NEUTRAL;
        } else if (whyIstopped.equals("collision")) {
            message = "Plane on flight with id " + this.flightId + " and name " + this.name + " has collided ";
            messageColor = MessagePanel.WARNING;
        } else if (whyIstopped.equals("squareCrashed")) {
            message = "Plane on flight with id " + this.flightId + " and name " + this.name + " has collided with a hill ";
            messageColor = MessagePanel.WARNING;
        } else if (whyIstopped.equals("fuel")) {
            message = "Plane on flight with id " + this.flightId + " and name " + this.name + " has no fuel ";
            messageColor = MessagePanel.WARNING;
        }
        if (gainHeightTimer != null) {
            gainHeightTimer.cancel();
            gainHeightTimer.purge();
        }
        hasStarted = false; //finished so we dont want it in available flights
        state = "failed";
        myPlane.clearMyTxtArea();
        Simulator.flightsPosition[0][threadId] = 1000;
        Simulator.flightsPosition[1][threadId] = 1000; //finished dont count me
        Simulator.frame.mapFrame.remove(myPlane);
        Simulator.frame.myMessagePanel.addNewMessage(message, messageColor);
        Simulator.frame.getContentPane().revalidate();
        Simulator.frame.getContentPane().repaint();
        Simulator.totalAircrafts.decrementAndGet(); // total aircrafts --
        Simulator.frame.myInfoBar.myRefresh();
    }


    private boolean willCrash(int currentHeight, int[] coordinates) {
        // Check height, should i crash?
        Square mySquare;
        int xOfSquare = coordinates[1] / 16;
        int yOfSquare = coordinates[0] / 16;
        mySquare = Simulator.frame.myMap.getSquareAt(xOfSquare, yOfSquare); //
        if (currentHeight < mySquare.getHght()) { // equality does not make sense
            //will crash
            return true;
        } else {
            //will not crash
            return false;
        }
    }

    private boolean willCrash(int currentHeight, int coordinatesX, int coordinatesY) {
        //coordinatesX , x coord of SQUARE not actual coordinations
        Square mySquare;
        int xOfSquare = coordinatesY;
        int yOfSquare = coordinatesX;
        mySquare = Simulator.frame.myMap.getSquareAt(xOfSquare, yOfSquare); //
        if (currentHeight < mySquare.getHght()) { //= not makes sense
            //will crash
            return true;
        } else {
            //will not crash
            return false;
        }
    }

    private void crashIt(Airplane myPlane) {
        // Failed... I crashed to a "wall"
        Simulator.collisions.incrementAndGet();
        Simulator.whyStopped[threadId] = "squareCrashed";
        makeExplosionGif(myPlane); //explosion gif
        flightEnded(myPlane);
        //need to return the thread afterwards
    }

    private void crashItFuel(Airplane myPlane) {
        // Failed... I'm out of fuel
        Simulator.collisions.incrementAndGet(); // collisions ++
        Simulator.whyStopped[threadId] = "fuel";
        makeExplosionGif(myPlane);
        flightEnded(myPlane);
        //need to return the thread afterwards
    }


    Airports startingAirport, endingAirport;



    private void runLoop() {
        int[] coordinates = new int[2];
        while (true) {
            if (phase != 4 && phase != 5) {
                coordinates = getNextCoordinations(planeClone); //chooses where to go, as first step
                planeClone.xPos = coordinates[0];
                planeClone.yPos = coordinates[1];
                //we must consume fuel for moving that pixel (depends on type)
                switch (typeOfPlane) {
                    case 1:  currentFuel -= SingleMotor.fuelPerPixel;
                        break;
                    case 2:  currentFuel -= Turboprop.fuelPerPixel;
                        break;
                    case 3:  currentFuel -= Jet.fuelPerPixel;
                        break;
                }
                if (currentFuel <= 0) {
                    crashItFuel(planeClone);
                    return;
                }
            } else if (phase == 4) {
                // do nothing height will be managed by the proper timer
            }
            if (willCrash(planeClone.height, coordinates)) {
                System.out.print(planeClone.height);
                System.out.println(" :the height I collided");
                crashIt(planeClone);
                return;
            }
            if (phase == 5) {
                flightSucceed(planeClone);
                return;
            } else {
                planeClone.orientation = heading;
                ArbitratorClient client = new ArbitratorClient(planeClone.height, coordinates, threadId);
                client.start();
                try {
                    client.join();
                } catch (Exception ex) {
                    System.out.println("Problem joining with client thread 1");
                    System.out.println(ex);
                    flightEnded(planeClone);
                    return;
                }
            }
            Simulator.frame.getContentPane().repaint();
            TimerTask wakeUpTask = new TimerTask() {
                public void run() {
                    synchronized (mySync) {
                        mySync.notify();
                    }
                }
            };
            if (phase == 2) {
                sleepTime = sleepTimeInPhase2;
            }
            else{
                switch (typeOfPlane) {
                    case 1:  sleepTime = SingleMotor.sleepTimeInPhase[phase];
                        break;
                    case 2:  sleepTime = Turboprop.sleepTimeInPhase[phase];
                        break;
                    case 3:  sleepTime = Jet.sleepTimeInPhase[phase];
                        break;
                }
            }
            wakeUpTimer.schedule(wakeUpTask, sleepTime);
            synchronized (mySync) {
                try {
                    mySync.wait();
                } catch (InterruptedException ex) {
                    wakeUpTimer.purge();
                    flightEnded(planeClone);
                    return;
                }
            }
        }
    }



    public void run() {
        hasStarted = false;
        state = "pending";
        // Start the timer, which will wake you when you should start
        TimerTask startingTimerTask = new TimerTask() {
            public void run() {
                synchronized (waitTillStart) {
                    waitTillStart.notify();
                }
            }
        };
        startingTimer = new Timer();
        startingTimer.schedule(startingTimerTask, startingTime * 60 * 1000 / 12); //sleeping time to start in ms
        synchronized (waitTillStart) {
            // block until start
            try {
                waitTillStart.wait();
            } catch (InterruptedException ex) {
                startingTimer.purge();
                return;
            }
        }
        lastSquareReached = false; // re initialize variables
        reachedOpenArea = false;
        gotHeightNeeded = false;
        gotHeightNeeded2 = false;
        phase = 1; //it will go 20 nm with lower velocity and it will gain height
        currentFuel = fFuel;
        flightStarting = true; //if restarted true
        Simulator.totalAircrafts.incrementAndGet(); // total aircrafts ++
        Simulator.frame.myInfoBar.myRefresh(); // show it
        Airports src = Simulator.frame.myMap.getAirportWithId(srcAirport); // get source airport
        Airports dest = Simulator.frame.myMap.getAirportWithId(destAirport); // get destination airport
        startingAirport = src;
        endingAirport = dest;
        Square mysquare = Simulator.frame.myMap.getSquareAt(src.getx(), src.gety());
        hasStarted = true; // ready to start
        state = "running";
        Simulator.frame.myMessagePanel.addNewMessage("Flight with id " + this.flightId + " and name " + this.name + " has started successfully ", MessagePanel.NEUTRAL);
        Simulator.frame.getContentPane().revalidate();
        Simulator.frame.getContentPane().repaint();
        // Calculate destination square globally, as the previous square of the destination airport (we achieve valid orientation of take-off)
        dests = calculatePreviousDestination();
        if (typeOfPlane == 1) {
            // first orientation should be the one that the source airport defines
            heading = validOrientation(src.getOrientation());
            // below we draw the airplane
            final SingleMotor myPlane = new SingleMotor((src.gety()) * 16 + 8, (src.getx()) * 16 + 8, validOrientation(src.getOrientation()), mysquare.getHght());
            myPlane.myFlightId = flightId;
            myPlane.myFlightName = name;
            Simulator.frame.mapFrame.add(myPlane, 1);
            Simulator.frame.mapFrame.add(Simulator.frame.myMap, -1);
            planeClone = myPlane; // up-casting. Now we can refer to it regardless of type
            // Set a timer that is responsible for gaining height over time
            TimerTask gainHeightTask = new TimerTask() {
                public void run() {
                    getHeightNeeded(planeClone, endingAirport);
                }
            };
            gainHeightTimer = new Timer();
            gainHeightTimer.scheduleAtFixedRate(gainHeightTask, 0, SingleMotor.sleepTimeInPhase[4]); //sleeping time for up-down 1 feet
            runLoop();
            return;
        } else if (typeOfPlane == 2) {
            heading = validOrientation(src.getOrientation());
            Turboprop myPlane = new Turboprop(src.gety() * 16 + 8, src.getx() * 16 + 8, validOrientation(src.getOrientation()), mysquare.getHght());
            myPlane.myFlightId = flightId;
            myPlane.myFlightName = name;
            Simulator.frame.mapFrame.add(myPlane, 1);
            Simulator.frame.mapFrame.add(Simulator.frame.myMap, -1);
            planeClone =  myPlane;
            TimerTask gainHeightTask = new TimerTask() {
                public void run() {
                    getHeightNeeded(planeClone, endingAirport);
                }
            };
            gainHeightTimer = new Timer();
            gainHeightTimer.scheduleAtFixedRate(gainHeightTask, 0, Turboprop.sleepTimeInPhase[4]); //sleeping time for up-down 1 feet
            runLoop();
            return;
        } else if (typeOfPlane == 3) {
            heading = validOrientation(src.getOrientation());
            Jet myPlane = new Jet(src.gety() * 16 + 8, src.getx() * 16 + 8, validOrientation(src.getOrientation()), mysquare.getHght());
            myPlane.myFlightId = flightId;
            myPlane.myFlightName = name;
            Simulator.frame.mapFrame.add(myPlane, 1);
            Simulator.frame.mapFrame.add(Simulator.frame.myMap, -1);
            planeClone = (Airplane) myPlane;
            TimerTask gainHeightTask = new TimerTask() {
                public void run() {
                    getHeightNeeded(planeClone, endingAirport);
                }
            };
            gainHeightTimer = new Timer();
            gainHeightTimer.scheduleAtFixedRate(gainHeightTask, 0, Jet.sleepTimeInPhase[4]);
            runLoop();
            return;
        }
    }


    private void getHeightNeeded(Airplane myPlane, Airports destAirport) {
        if (phase == 1 || phase == 2) { // should gain height
            if (myPlane.height == fHeight && !gotHeightNeeded) {
                gotHeightNeeded = true;
                Simulator.frame.myMessagePanel.addNewMessage("Flight with id " + this.flightId + " and name " + this.name + " has reached the desirable height for the flight ", MessagePanel.NEUTRAL);
            } else if (!(myPlane.height == fHeight)) {
                myPlane.height += 1;
            }
        } else if (phase == 3 || phase == 4) { // should lose height
            Square squareDestination = Simulator.frame.myMap.getSquareAt(destAirport.getx(), destAirport.gety());
            if (myPlane.height == squareDestination.getHght()) {
                gotHeightNeeded2 = true;
                if (phase == 4) phase = 5; // if i have reached to airport i am ready
            } else {
                myPlane.height -= 1;
            }
        }
    }

    private int[] getNextCoordinations(Airplane myPlane) {
        int srcX = myPlane.xPos;
        int srcY = myPlane.yPos;
        int coordinate[] = new int[2];
        if ((myPlane.xPos % 8 == 0) && (myPlane.yPos % 8 == 0) && ((myPlane.xPos / 8) % 2 == 1) && ((myPlane.yPos / 8) % 2 == 1)) { //im in the middle of a square (means i should re validate)
            if (!flightStarting) {
                if (!reachedOpenArea) {
                    reachedOpenArea = true;
                    phase = 2;
                }
                makeNewDecision(myPlane);
            } else {
                //here we make decision for the first orientation (we want the orientation of the source airport)
                heading = validOrientation(startingAirport.getOrientation());
                flightStarting = false;
            }
        }
        // calculate x, y coordinates depending on heading variable
        if (heading == "w") {
            srcX -= 1;
        } else if (heading == "e") {
            srcX += 1;
        } else if (heading == "s") {
            srcY += 1;
        } else if (heading == "n") {
            srcY -= 1;
        } else if (heading == "se") {
            srcX += 1;
            srcY += 1;
        } else if (heading == "sw") {
            srcX -= 1;
            srcY += 1;
        } else if (heading == "ne") {
            srcX += 1;
            srcY -= 1;
        } else if (heading == "nw") {
            srcX -= 1;
            srcY -= 1;
        }
        coordinate[0] = srcX;
        coordinate[1] = srcY;
        return coordinate;
    }


    private String getRandomDecision() {
        // pick a choise randomly
        String[] possibilities = {"w", "e", "s", "n", "se", "sw", "ne", "nw"};
        int idx = new Random().nextInt(possibilities.length);
        String random = (possibilities[idx]);
        return random;
    }


    private int[] calculatePreviousDestination() {
        // **future change: compute only once!!**
        Airports dest = Simulator.frame.myMap.getAirportWithId(destAirport);
        String destOrientation = validOrientation(dest.getOrientation());
        int[] destXY = new int[2];
        destXY[0] = dest.gety();
        destXY[1] = dest.getx();
        if (destOrientation == "w") {
            destXY[0] -= 1;
        } else if (destOrientation == "e") {
            destXY[0] += 1;
        } else if (destOrientation == "s") {
            destXY[1] += 1;
        } else if (destOrientation == "n") {
            destXY[1] -= 1;
        }
        if (destXY[0] < 0 || destXY[1] < 0 || destXY[0] > 60 || destXY[1] > 30) {
            //it will go out of bounds!!
            System.out.println("FLIGHT WITH ID" + flightId + "WILL GO OUT OF BOUNDS BECAUSE OF DESTINATION' AIRPORTS' ORIENTATION");
            System.exit(0);
        }
        return destXY;
    }


    private void makeNewDecision(Airplane myPlane) {
        Airports dest = Simulator.frame.myMap.getAirportWithId(destAirport);
        int destX = dests[1];
        int destY = dests[0];
        int finalDestX = dest.getx();
        int finalDestY = dest.gety();
        int srcX = myPlane.xPos / 16;
        int srcY = myPlane.yPos / 16; //get squares
        int nextX = srcX;
        int nextY = srcY;
        int coordinate[] = new int[2];

        //srcX X of square that i am
        //srcY Y of square that i am

        //nextX X of square that ill go
        //nextY Y of square that ill go

        //first look if you are near Airport and then look if you reached destination
        //this way, you are forced to reach previous square first in order to agree with airport's orientation
        if (Math.abs(destX - srcY) + Math.abs(destY - srcX) == 0) {//has reached previous square in order to reach airport through square with valid orientation
            phase = 3; //need to slow down, i am near airport
            lastSquareReached = true;
            if (validOrientation(dest.getOrientation()) == "w") heading = "e";
            else if (validOrientation(dest.getOrientation()) == "e") heading = "w";
            else if (validOrientation(dest.getOrientation()) == "n") heading = "s";
            else if (validOrientation(dest.getOrientation()) == "s") heading = "n";
            return;
        } else if (Math.abs(finalDestX - srcY) + Math.abs(finalDestY - srcX) == 0 && lastSquareReached) {//has reached
            phase = 4;
            if (gotHeightNeeded2) phase = 5;
            coordinate[0] = 0;
            coordinate[1] = 0; //reached
            return;
        }
        //8 options choose the best only if not crashes and not the previous
        String bestChoise = "";
        boolean foundCoordination = false; // Head to the closest, if you won't crash and it is not your previous choice (opposite of heading variable)
        //crash if there is no ot
        // her choice
        int cost = Math.abs(destX - srcY) + Math.abs(destY - srcX) + 10; //from where i am plus 5 (impossible)

        if (((Math.abs(destX - srcY) + Math.abs(destY - (srcX - 1))) < cost) && (!willCrash(myPlane.height, srcX - 1, srcY)) && !heading.equals("e")) {
            foundCoordination = true;
            bestChoise = "w";
            nextX = srcX - 1;
            nextY = srcY;
            cost = Math.abs(destX - srcY) + Math.abs(destY - (srcX - 1));
        }
        if (((Math.abs(destX - srcY) + Math.abs(destY - (srcX + 1))) < cost) && (!willCrash(myPlane.height, srcX + 1, srcY)) && !heading.equals("w")) {
            foundCoordination = true;
            bestChoise = "e";
            nextX = srcX + 1;
            nextY = srcY;
            cost = Math.abs(destX - srcY) + Math.abs(destY - (srcX + 1));
        }
        if (((Math.abs(destX - (srcY - 1)) + Math.abs(destY - srcX)) < cost) && (!willCrash(myPlane.height, srcX, srcY - 1)) && !heading.equals("s")) {
            foundCoordination = true;
            bestChoise = "n";
            nextX = srcX;
            nextY = srcY - 1;
            cost = Math.abs(destX - (srcY - 1)) + Math.abs(destY - srcX);
        }
        if (((Math.abs(destX - (srcY + 1)) + Math.abs(destY - srcX)) < cost) && (!willCrash(myPlane.height, srcX, srcY + 1)) && !heading.equals("n")) {
            foundCoordination = true;
            bestChoise = "s";
            nextX = srcX;
            nextY = srcY + 1;
            cost = Math.abs(destX - (srcY + 1)) + Math.abs(destY - srcX);
        }
        if (((Math.abs(destX - (srcY + 1)) + Math.abs(destY - (srcX - 1))) < cost) && (!willCrash(myPlane.height, srcX - 1, srcY + 1)) && !heading.equals("ne")) {
            foundCoordination = true;
            bestChoise = "sw";
            nextX = srcX - 1;
            nextY = srcY + 1;
            cost = Math.abs(destX - (srcY + 1)) + Math.abs(destY - (srcX - 1));
        }
        if (((Math.abs(destX - (srcY - 1)) + Math.abs(destY - (srcX - 1))) < cost) && (!willCrash(myPlane.height, srcX - 1, srcY - 1)) && !heading.equals("se")) {
            foundCoordination = true;
            bestChoise = "nw";
            nextX = srcX - 1;
            nextY = srcY - 1;
            cost = Math.abs(destX - (srcY - 1)) + Math.abs(destY - (srcX - 1));
        }
        if (((Math.abs(destX - (srcY + 1)) + Math.abs(destY - (srcX + 1))) < cost) && (!willCrash(myPlane.height, srcX + 1, srcY + 1)) && !heading.equals("nw")) {

            foundCoordination = true;
            bestChoise = "se";
            nextX = srcX + 1;
            nextY = srcY + 1;
            cost = Math.abs(destX - (srcY + 1)) + Math.abs(destY - (srcX + 1));
        }
        if (((Math.abs(destX - (srcY - 1)) + Math.abs(destY - (srcX + 1))) < cost) && (!willCrash(myPlane.height, srcX + 1, srcY - 1)) && !heading.equals("sw")) {
            foundCoordination = true;
            bestChoise = "ne";
            nextX = srcX + 1;
            nextY = srcY - 1;
            cost = Math.abs(destX - (srcY - 1)) + Math.abs(destY - (srcX + 1));
        }

        if (foundCoordination) {
            heading = bestChoise;
            coordinate[0] = nextX;
            coordinate[1] = nextY;
        } else { //couldn find heading value that doesnt make me collide nor was my previous choice
            heading = getRandomDecision(); // make random decision: choose from crash or previous decision
        }
    }


    public int getPhase() {
        return phase;
    }

    private void makeExplosionGif(Airplane myPlane) {
          /*
             Make it explode
         */
        try {
            JLabel labelll = new JLabel();
            int xPos = myPlane.xPos;
            int yPos = myPlane.yPos;
            labelll.setBounds(xPos - 16, yPos - 16, 30, 30);

            BufferedImage img = null;
            try {
                img = ImageIO.read(new File("explosion.gif"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Image dimg = img.getScaledInstance(labelll.getWidth(), labelll.getHeight(),
                    Image.SCALE_SMOOTH);
            Icon imageIcon = new ImageIcon(dimg);
            labelll.setIcon(imageIcon);


            Simulator.frame.mapFrame.add(labelll, 1);
            Simulator.frame.mapFrame.add(Simulator.frame.myMap, -1);

            Simulator.frame.mapFrame.revalidate();
            Simulator.frame.mapFrame.repaint();
            //Simulator.frame.getContentPane().repaint();

            try {
                Thread.sleep(400);
            } catch (Exception ex) {
                System.out.println("Someone interrupted me during collision");
            }
            Simulator.frame.mapFrame.remove(labelll);
            Simulator.frame.getContentPane().revalidate();
            Simulator.frame.getContentPane().repaint();
        } catch (Exception ex) {
            System.out.println("Explosion gif problem");
            System.out.println(ex);
        }
        /*
        Make it explode
         */
    }
}
