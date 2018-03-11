/**
 * Created by Παναγιώτης on 23/11/2017.
 */

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileReader;


public class Map extends JPanel {

    private Square[][] squareHolder = new Square[30][60];
    private Airports[] airportHolder = new Airports[100]; //100 max airports
    private String mapId;
    public int numberOfairports;


    public Map(String MAPID) {
        super(new GridLayout(30, 60, 0, 0));
        this.mapId = MAPID;
        this.fillMapWithSquares();
        this.fillMapWithAirports();
        this.setPreferredSize(new Dimension(960, 480));////480
    }


    private void fillMapWithSquares() {
        String lineOfNumbers;
        String[] line = new String[30];
        int height, red, green, blue;
        try {
            Scanner f = new Scanner(new FileReader("world_" + mapId));
            for (int i = 0; i < 30; i++) {
                lineOfNumbers = f.nextLine();
                line = lineOfNumbers.split(",");
                for (int j = 0; j < 60; j++) {
                    // read map and fill squares with colors also fill squareHolder
                    red = blue = green = 0;
                    height = Integer.parseInt(line[j]);
                    if (height == 0) {
                        red = 0;
                        green = 0;
                        blue = 255;
                    } else if (height > 0 && height <= 130) {
                        red = 60;
                        green = 179;
                        blue = 113;
                    } else if (height > 130 && height <= 200) {
                        red = 20;
                        green = 159;
                        blue = 80;
                    } else if (height > 200 && height <= 300) {
                        red = 46;
                        green = 139;
                        blue = 87;
                    } else if (height > 300 && height <= 400) {
                        red = 40;
                        green = 120;
                        blue = 90;
                    } else if (height > 400 && height <= 600) {
                        red = 34;
                        green = 139;
                        blue = 34;
                    } else if (height > 600 && height <= 700) {
                        red = 34;
                        green = 160;
                        blue = 34;
                    } else if (height > 700 && height <= 1500) {
                        red = 222;
                        green = 184;
                        blue = 135;
                    } else if (height > 1500 && height <= 2500) {
                        red = 205;
                        green = 133;
                        blue = 63;
                    } else if (height > 2500 && height <= 3500) {
                        red = 205;
                        green = 133;
                        blue = 34;
                    } else if (height > 3500) {
                        red = 145;
                        green = 80;
                        blue = 20;
                    }
                    squareHolder[i][j] = new Square(height, red, green, blue, null);
                    this.add(squareHolder[i][j]);
                }
            }
            f.close();
        } catch (FileNotFoundException ex) {
            if (!mapId.equals("")) {
                System.out.println("cannot find world_" + mapId);
            }

        }
    }


    private void fillMapWithAirports() {
        // fill map object with airports and fill airportHolder
        String lineOfNumbers;
        String[] line = new String[8];
        int id, x, y;
        String name;
        byte orientation, type;
        boolean open;
        try {
            Scanner f = new Scanner(new FileReader("airports_" + mapId));
            int i = 0;
            while (f.hasNextLine()) {
                lineOfNumbers = f.nextLine();
                line = lineOfNumbers.split(",");
                id = Integer.parseInt(line[0]);
                x = Integer.parseInt(line[1]);
                y = Integer.parseInt(line[2]);
                name = line[3];
                orientation = (byte) Integer.parseInt(line[4]);
                type = (byte) Integer.parseInt(line[5]);
                if (line[6].equals("1")) {
                    open = true;
                } else {
                    open = false;
                }
                airportHolder[i] = new Airports(id, x, y, name, orientation, type, open);
                squareHolder[x][y].setAirport(airportHolder[i]); // Actually set connected property of square in order to paint the airport in the middle of a square
                i++;

            }
            numberOfairports = i;
            f.close();
        } catch (FileNotFoundException ex) {
            if (!mapId.equals("")) {
                System.out.println("cannot find airport_" + mapId);
            }
        }

    }


    public Airports getAirportAt(int pos) {
        return airportHolder[pos];
    }

    public Airports getAirportWithId(int id) {
        for (int i = 0; i < numberOfairports; i++) {
            if (airportHolder[i].getId() == id) return airportHolder[i];
        }
        return null;
    }

    public Square getSquareAt(int x, int y) {
        return squareHolder[x][y];
    }

}
