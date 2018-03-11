import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Παναγιώτης on 24/11/2017.
 */

public class Airports extends JPanel {
    private int id;
    private int x;
    private int y;
    private String name;
    private byte orientation; //1 -> south, 2 -> east, 3 -> north, 4 -> west
    private byte type; //1 -> single motor, 2 -> turboprop & jet, 3 -> all
    private boolean open; // true open;

    // airports are drawn through Square class, when connected to an airport
    public Airports(int id, int x, int y, String name, byte orientation, byte type, boolean open) {
        this.id = id;
        this.x = x;
        //airports are in the middle of a Square by definition
        this.y = y;
        this.name = name;
        this.orientation = orientation;
        this.type = type;
        this.open = open;
    }


    public String getStringOrientation() {
        String orientation = "";
        if (this.orientation == 1) orientation = "North";
        if (this.orientation == 2) orientation = "East";
        if (this.orientation == 3) orientation = "South";
        if (this.orientation == 4) orientation = "West";
        return orientation;
    }


    public int getId() {
        return this.id;
    }

    public int getx() {
        return this.x;
    }

    public int gety() {
        return this.y;
    }

    public String getAirportName() {
        return this.name;
    }

    public Byte getOrientation() {
        return this.orientation;
    }

    public Byte getType() {
        return this.type;
    }

    public Boolean getOpen() {
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }


}
