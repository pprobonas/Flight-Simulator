
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by Παναγιώτης on 26/11/2017.
 */
// The top panel, below jmenubar
// Call myRefresh in order to take parameters of simulation and print them on top
public class InfoBar extends JPanel {
    JLabel lbl1 = new JLabel("Simulated Time: " + "0" + ":" + "0" + ":" + "0.0");
    JLabel lbl2 = new JLabel("Total Aircrafts: " + (Simulator.totalAircrafts));
    JLabel lbl3 = new JLabel("Collisions: " + (Simulator.collisions));
    JLabel lbl4 = new JLabel("Landings: " + (Simulator.landings));

    public InfoBar() {
        lbl1.setFont(new Font("Courier New", Font.ITALIC, 12));
        lbl2.setFont(new Font("Courier New", Font.ITALIC, 12));
        lbl3.setFont(new Font("Courier New", Font.ITALIC, 12));
        lbl4.setFont(new Font("Courier New", Font.ITALIC, 12));
        //this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setLayout(new GridLayout(1, 4));
        this.setBackground(new Color(0, 20, 30, 10));
        this.add(lbl1);
        this.add(lbl2);
        this.add(lbl3);
        this.add(lbl4);
        this.setPreferredSize(new Dimension(1260, 16));
    }

    public void myRefresh() {
        long hours = (long) Simulator.simulatedTime / 3600;
        long minutes = (long) (Simulator.simulatedTime - hours * 3600) / 60;
        double seconds = (double) Math.round((Simulator.simulatedTime - hours * 3600 - minutes * 60) * 100) / 100;
        lbl1.setText("Simulated Time: " + hours + ":" + minutes + ":" + seconds);
        lbl2.setText("Total Aircrafts: " + (Simulator.totalAircrafts));
        lbl3.setText("Collisions: " + (Simulator.collisions));
        lbl4.setText("Landings: " + (Simulator.landings));
        Simulator.frame.getContentPane().revalidate();
        Simulator.frame.getContentPane().repaint();
    }


}





