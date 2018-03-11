import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Παναγιώτης on 6/12/2017.
 */
public class Arbitrator extends Thread {
    private Socket socket;
    private JLabel labelForExplosionGif;
    Timer stopExplodingTimer;

    public Arbitrator(Socket socket){
        // take already opened socket from arbitratorServer
        this.socket = socket;
    }

    private void destroy(int threadId1,int threadId2){
        //destroy planes safely
        Simulator.whyStopped[threadId1] = "collision"; // explain to them that they have been destroyed because the collided with each other
        Simulator.whyStopped[threadId2] = "collision";
        Simulator.collisions.incrementAndGet();
        Simulator.collisions.incrementAndGet();
        // System.out.println("crash");
        Simulator.frame.myInfoBar.myRefresh();
        // interrupt the first one
        Simulator.currentFlights[threadId1].interrupt();
        try {
            Simulator.currentFlights[threadId1].join();
        }
        catch(Exception ex) {
            System.out.println("Problem joining with thread: " + threadId1);
        }
        // interrupt the second one
        Simulator.currentFlights[threadId2].interrupt();
        try {
            Simulator.currentFlights[threadId2].join();
        }
        catch(Exception ex) {
            System.out.println("Problem joining with thread: " + threadId2);
        }

        /*
            Start of explosion
        */
        try {
            labelForExplosionGif = new JLabel();
            int xPos = Simulator.availableFlights[threadId1].planeClone.xPos;
            int yPos = Simulator.availableFlights[threadId1].planeClone.yPos;
            labelForExplosionGif.setBounds(xPos - 16,yPos - 16,30,30);

            BufferedImage img = null;
            try {
                img = ImageIO.read(new File("explosion.gif"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // scale it properly
            Image dimg = img.getScaledInstance(labelForExplosionGif.getWidth(), labelForExplosionGif.getHeight(),
                    Image.SCALE_SMOOTH);
            Icon imageIcon = new ImageIcon(dimg);
            labelForExplosionGif.setIcon(imageIcon);
            Simulator.frame.mapFrame.add(labelForExplosionGif,1); // make it visible
            Simulator.frame.mapFrame.add(Simulator.frame.myMap,-1);
            Simulator.frame.mapFrame.revalidate();
            Simulator.frame.mapFrame.repaint();
            stopExplodingTimer = new Timer(); // set a timer to clean this gif after a second
            TimerTask stopExplodingTask = new TimerTask() {
                public void run() {
                    Simulator.frame.mapFrame.remove(labelForExplosionGif);
                    Simulator.frame.getContentPane().revalidate();
                    Simulator.frame.getContentPane().repaint();
                }
            };
            stopExplodingTimer.schedule(stopExplodingTask, 1000); //sleeping time to start in ms
        }
        catch (Exception ex) {
            System.out.println("Explosion gif problem");
            System.out.println(ex);
        }
         /*
            End of explosion
        */
    }

    private void doCheck(int planeHeight, int[] coordinates, int servingThread){
        //check table Simulator.flightsPosition with Simulator.numberofflights
        //serving thread updated its position
        int xDistance, yDistance, distance, hDistance;
        Simulator.flightsPosition[0][servingThread] = coordinates[0];
        Simulator.flightsPosition[1][servingThread] = coordinates[1];
        Simulator.flightsPosition[2][servingThread] = planeHeight;
        for (int i=0; i<Simulator.numberOfFlights; i++){
            // if flight i has started (!=1000) and i have not finished and i is not myself...
            if (Simulator.flightsPosition[0][i] != 1000 && Simulator.flightsPosition[0][servingThread] != 1000 && i != servingThread ){
                //check these flights they might collide in coordinates
                xDistance = Math.abs(Simulator.flightsPosition[0][i] - Simulator.flightsPosition[0][servingThread]);
                yDistance = Math.abs(Simulator.flightsPosition[1][i] - Simulator.flightsPosition[1][servingThread]);
                hDistance = Math.abs(Simulator.flightsPosition[2][i] - Simulator.flightsPosition[2][servingThread]);
                distance = xDistance + yDistance;
                if (distance <= 2 && hDistance < 500){ // we are 2 pixels close and our height dif is less than 500 feets
                    if (!Simulator.availableFlights[i].hasStarted) return;
                    destroy(servingThread, i); // destroy those 2 flights
                }
            }

        }
    }


    public void run() {
        DataInputStream in = null;
        try {
            // takes input from the client socket (a flight)
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            int servingThread;
            int planeHeight;
            String line;
            int[] coordinates = new int[2];
            String[] lineRead = new String[3];
            try {
                line = in.readUTF();
                // System.out.println(line);
                // each flight should say: its x, y coordinates, which thread is it, its height.
                lineRead = line.split(" ");
                coordinates[0] = Integer.parseInt(lineRead[0]);
                coordinates[1] = Integer.parseInt(lineRead[1]);
                servingThread = Integer.parseInt(lineRead[2]);
                planeHeight = Integer.parseInt(lineRead[3]);
                doCheck(planeHeight, coordinates, servingThread); //check if there is/are a crash(es) and if there is/are stop it/them...
            } catch (IOException i) {
                System.out.println(i);
            }
            // close connection
            socket.close();
            in.close();
        } catch (IOException i) {
            System.out.println(i);
        }



    }
}